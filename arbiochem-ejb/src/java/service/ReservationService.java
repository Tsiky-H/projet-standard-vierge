package service;

import bean.ClassMAPTable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
// import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import reservation.Reservation;
import reservation.ReservationDetail;
import reservation.ReservationPlace;
import user.UserEJB;
import utilitaire.UtilDB;

/** Opérations transactionnelles de création d'une réservation non payée. */
public class ReservationService {

    private ReservationService() { }

    public static Reservation creerReservation(UserEJB utilisateur, String numeroReservation,
            String idClient, String idModePaiementPrevu, Timestamp dateExpiration,
            List<PlaceChoisie> placesChoisies) throws Exception {
        if (utilisateur == null) throw new IllegalArgumentException("Utilisateur APJ obligatoire.");
        if (numeroReservation == null || numeroReservation.trim().isEmpty()) {
            throw new IllegalArgumentException("Le numéro de réservation est obligatoire.");
        }
        if (dateExpiration == null || !dateExpiration.after(new Timestamp(System.currentTimeMillis()))) {
            throw new IllegalArgumentException("L'échéance doit être dans le futur.");
        }
        if (placesChoisies == null || placesChoisies.isEmpty()) {
            throw new IllegalArgumentException("Sélectionnez au moins une place.");
        }

        Map<String, List<PlaceChoisie>> parSeance = new TreeMap<>();
        Set<String> couples = new HashSet<>();
        for (PlaceChoisie choix : placesChoisies) {
            if (choix == null || choix.getIdSeance() == null || choix.getIdPlace() == null) {
                throw new IllegalArgumentException("Chaque place doit être liée à une séance.");
            }
            String couple = choix.getIdSeance() + "\u0000" + choix.getIdPlace();
            if (!couples.add(couple)) throw new IllegalArgumentException("Une place est sélectionnée plusieurs fois.");
            parSeance.computeIfAbsent(choix.getIdSeance(), k -> new ArrayList<>()).add(choix);
        }
        Comparator<PlaceChoisie> ordrePlaces = Comparator.comparing(PlaceChoisie::getIdPlace);
        for (List<PlaceChoisie> places : parSeance.values()) Collections.sort(places, ordrePlaces);

        Connection connexion = null;
        try {
            connexion = new UtilDB().GetConn();
            connexion.setAutoCommit(false);

            // Garder un ordre cohérent : séances, puis places triées par séance/place.
            for (String idSeance : parSeance.keySet()) verifierSeanceOuverte(connexion, idSeance);

            // Les expirations peuvent libérer plusieurs places d'une même réservation.
            // On ajoute donc ces places à la liste des verrous et les prend toutes dans
            // un ordre global séance/place avant de modifier les statuts.
            Set<String> reservationsAExpirer = new TreeSet<>(trouverReservationsEchues(connexion, parSeance));
            Map<String, PlaceChoisie> placesAVerrouiller = new TreeMap<>();
            for (List<PlaceChoisie> places : parSeance.values()) {
                for (PlaceChoisie choix : places) ajouterPlace(placesAVerrouiller, choix);
            }
            for (String idReservation : reservationsAExpirer) {
                for (PlaceChoisie place : trouverPlacesReservation(connexion, idReservation)) {
                    ajouterPlace(placesAVerrouiller, place);
                }
            }
            for (PlaceChoisie choix : placesAVerrouiller.values()) {
                verrouillerPlace(connexion, choix.getIdPlace());
            }
            for (String idReservation : reservationsAExpirer) {
                expirerReservationSiEchue(connexion, idReservation);
            }

            List<PlaceVerrouillee> selection = new ArrayList<>();
            BigDecimal montantTotal = BigDecimal.ZERO;
            for (Map.Entry<String, List<PlaceChoisie>> entree : parSeance.entrySet()) {
                for (PlaceChoisie choix : entree.getValue()) {
                    PlaceVerrouillee place = lirePrixEtVerifierDisponibilite(connexion,
                            entree.getKey(), choix.getIdPlace());
                    selection.add(place);
                    montantTotal = montantTotal.add(place.prix);
                }
            }

            Timestamp maintenant = new Timestamp(System.currentTimeMillis());
            Reservation reservation = new Reservation();
            reservation.setNumero_reservation(numeroReservation.trim());
            reservation.setIdclient(idClient);
            reservation.setIdmodepaiement_prevu(idModePaiementPrevu);
            reservation.setDate_reservation(maintenant);
            reservation.setDate_expiration(dateExpiration);
            reservation.setStatut("EN_ATTENTE");
            reservation.setNb_places_total(selection.size());
            reservation.setMontant_total(montantTotal.doubleValue());
            utilisateur.createObject(reservation, connexion);

            Map<String, List<PlaceVerrouillee>> parSeanceVerrouillee = new TreeMap<>();
            for (PlaceVerrouillee place : selection) {
                parSeanceVerrouillee.computeIfAbsent(place.idSeance, k -> new ArrayList<>()).add(place);
            }

            for (Map.Entry<String, List<PlaceVerrouillee>> entree : parSeanceVerrouillee.entrySet()) {
                BigDecimal montantLigne = BigDecimal.ZERO;
                for (PlaceVerrouillee place : entree.getValue()) montantLigne = montantLigne.add(place.prix);

                ReservationDetail detail = new ReservationDetail();
                detail.setIdreservation(reservation.getIdreservation());
                detail.setIdseance(entree.getKey());
                detail.setNb_places(entree.getValue().size());
                detail.setMontant_ligne(montantLigne.doubleValue());
                detail.setStatut("EN_ATTENTE");
                utilisateur.createObject(detail, connexion);

                for (PlaceVerrouillee place : entree.getValue()) {
                    ReservationPlace lignePlace = new ReservationPlace();
                    lignePlace.setIdreservation_detail(detail.getIdreservation_detail());
                    lignePlace.setIdseance(place.idSeance);
                    lignePlace.setIdplace(place.idPlace);
                    lignePlace.setIdcategorieplace(place.idCategorie);
                    lignePlace.setPrix_unitaire(place.prix.doubleValue());
                    lignePlace.setStatut("BLOQUEE");
                    lignePlace.setDate_blocage(maintenant);
                    lignePlace.setDate_expiration(dateExpiration);
                    utilisateur.createObject(lignePlace, connexion);
                }
            }

            connexion.commit();
            return reservation;
        } catch (Exception e) {
            if (connexion != null) connexion.rollback();
            throw e;
        } finally {
            if (connexion != null) connexion.close();
        }
    }

    private static void verifierSeanceOuverte(Connection connexion, String idSeance) throws Exception {
        String sql = "SELECT STATUT FROM CINE_SEANCE WHERE IDSEANCE = ? FOR UPDATE";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idSeance);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Séance introuvable: " + idSeance);
                String statut = rs.getString(1);
                if (!"PLANIFIEE".equals(statut) && !"OUVERTE".equals(statut)) {
                    throw new Exception("La séance n'est pas ouverte à la réservation.");
                }
            }
        }
    }

    private static void verrouillerPlace(Connection connexion, String idPlace) throws Exception {
        String sql = "SELECT IDPLACE FROM CINE_PLACE WHERE IDPLACE = ? AND ACTIF = 1 FOR UPDATE";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idPlace);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Place introuvable ou inactive: " + idPlace);
            }
        }
    }

    private static PlaceVerrouillee lirePrixEtVerifierDisponibilite(Connection connexion,
            String idSeance, String idPlace) throws Exception {
        String sql = "SELECT p.IDCATEGORIEPLACE, cp.PRIX_BASE "
                + "FROM CINE_PLACE p JOIN CINE_SEANCE s ON s.IDSALLE = p.IDSALLE "
                + "JOIN CINE_CATEGORIE_PLACE cp ON cp.IDCATEGORIEPLACE = p.IDCATEGORIEPLACE "
                + "WHERE s.IDSEANCE = ? AND p.IDPLACE = ? AND cp.ACTIF = 1 "
                + "AND NOT EXISTS (SELECT 1 FROM CINE_RESERVATION_PLACE rp "
                + "WHERE rp.IDSEANCE = s.IDSEANCE AND rp.IDPLACE = p.IDPLACE "
                + "AND rp.STATUT IN ('BLOQUEE','ACHETEE')) "
                + "AND NOT EXISTS (SELECT 1 FROM VENTEDETAILS vd "
                + "WHERE vd.IDSEANCE = s.IDSEANCE AND vd.IDPLACE = p.IDPLACE "
                + "AND vd.STATUT IN ('BLOQUEE','ACHETEE'))";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idSeance);
            ps.setString(2, idPlace);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("La place n'appartient pas à cette séance ou n'est plus disponible.");
                return new PlaceVerrouillee(idSeance, idPlace, rs.getString(1), rs.getBigDecimal(2));
            }
        }
    }

    private static Set<String> trouverReservationsEchues(Connection connexion,
            Map<String, List<PlaceChoisie>> selection) throws Exception {
        Set<String> ids = new TreeSet<>();
        String sql = "SELECT DISTINCT r.IDRESERVATION FROM CINE_RESERVATION_PLACE rp "
                + "JOIN CINE_RESERVATION_DETAIL rd ON rd.IDRESERVATION_DETAIL = rp.IDRESERVATION_DETAIL "
                + "JOIN CINE_RESERVATION r ON r.IDRESERVATION = rd.IDRESERVATION "
                + "WHERE rp.IDSEANCE = ? AND rp.IDPLACE = ? AND rp.STATUT = 'BLOQUEE' "
                + "AND rp.DATE_EXPIRATION <= SYSDATE AND r.STATUT = 'EN_ATTENTE' "
                + "AND NOT EXISTS (SELECT 1 FROM CINE_PAIEMENT p WHERE p.IDRESERVATION = r.IDRESERVATION "
                + "AND p.STATUT = 'VALIDE' AND p.DATY <= r.DATE_EXPIRATION)";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            for (Map.Entry<String, List<PlaceChoisie>> entree : selection.entrySet()) {
                for (PlaceChoisie choix : entree.getValue()) {
                    ps.setString(1, entree.getKey());
                    ps.setString(2, choix.getIdPlace());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) ids.add(rs.getString(1));
                    }
                }
            }
        }
        return ids;
    }

    private static List<PlaceChoisie> trouverPlacesReservation(Connection connexion,
            String idReservation) throws Exception {
        List<PlaceChoisie> places = new ArrayList<>();
        String sql = "SELECT rp.IDSEANCE, rp.IDPLACE FROM CINE_RESERVATION_PLACE rp "
                + "JOIN CINE_RESERVATION_DETAIL rd ON rd.IDRESERVATION_DETAIL = rp.IDRESERVATION_DETAIL "
                + "WHERE rd.IDRESERVATION = ? AND rp.STATUT IN ('BLOQUEE','ACHETEE') "
                + "ORDER BY rp.IDSEANCE, rp.IDPLACE";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idReservation);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) places.add(new PlaceChoisie(rs.getString(1), rs.getString(2)));
            }
        }
        return places;
    }

    private static void expirerReservationSiEchue(Connection connexion,
            String idReservation) throws Exception {
        String reservationSql = "UPDATE CINE_RESERVATION r SET STATUT = 'EXPIREE' "
                + "WHERE r.IDRESERVATION = ? AND r.STATUT = 'EN_ATTENTE' "
                + "AND r.DATE_EXPIRATION <= SYSDATE "
                + "AND NOT EXISTS (SELECT 1 FROM CINE_PAIEMENT p "
                + "WHERE p.IDRESERVATION = r.IDRESERVATION AND p.STATUT = 'VALIDE' "
                + "AND p.DATY <= r.DATE_EXPIRATION)";
        int modifiees;
        try (PreparedStatement ps = connexion.prepareStatement(reservationSql)) {
            ps.setString(1, idReservation);
            modifiees = ps.executeUpdate();
        }
        if (modifiees == 0) return;

        String detailsSql = "UPDATE CINE_RESERVATION_DETAIL SET STATUT = 'EXPIREE' "
                + "WHERE IDRESERVATION = ? AND STATUT = 'EN_ATTENTE'";
        try (PreparedStatement ps = connexion.prepareStatement(detailsSql)) {
            ps.setString(1, idReservation);
            ps.executeUpdate();
        }

        String placesSql = "UPDATE CINE_RESERVATION_PLACE rp SET STATUT = 'EXPIREE' "
                + "WHERE rp.STATUT = 'BLOQUEE' AND rp.IDRESERVATION_DETAIL IN "
                + "(SELECT rd.IDRESERVATION_DETAIL FROM CINE_RESERVATION_DETAIL rd WHERE rd.IDRESERVATION = ?)";
        try (PreparedStatement ps = connexion.prepareStatement(placesSql)) {
            ps.setString(1, idReservation);
            ps.executeUpdate();
        }
    }

    private static void ajouterPlace(Map<String, PlaceChoisie> places, PlaceChoisie choix) {
        String cle = choix.getIdSeance() + "\u0000" + choix.getIdPlace();
        places.putIfAbsent(cle, choix);
    }

    public static final class PlaceChoisie {
        private final String idSeance;
        private final String idPlace;

        public PlaceChoisie(String idSeance, String idPlace) {
            this.idSeance = idSeance;
            this.idPlace = idPlace;
        }

        public String getIdSeance() { return idSeance; }
        public String getIdPlace() { return idPlace; }
    }

    private static final class PlaceVerrouillee {
        private final String idSeance;
        private final String idPlace;
        private final String idCategorie;
        private final BigDecimal prix;

        private PlaceVerrouillee(String idSeance, String idPlace, String idCategorie, BigDecimal prix) {
            this.idSeance = idSeance;
            this.idPlace = idPlace;
            this.idCategorie = idCategorie;
            this.prix = prix;
        }
    }
}
