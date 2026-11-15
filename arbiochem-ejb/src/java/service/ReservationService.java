package service;

import cinema.PlaceSeanceDispo;
import cinema.ReglesCinema;
import reservation.Reservation;
import reservation.ReservationDetail;
import reservation.ReservationPlace;
import user.UserEJB;
import vente.Vente;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import utilitaire.UtilDB;

/** Opérations métier liées aux réservations de séances. */
public class ReservationService {

    public ReservationService() { }

    /** Crée atomiquement une réservation, ses places bloquées, ses détails et sa vente brouillon. */
    public Reservation creerReservation(UserEJB userEJB, Reservation reservation,
            Collection<PlaceSeanceDispo> placesSelectionnees) throws Exception {
        if (userEJB == null) {
            throw new IllegalArgumentException("Le service utilisateur est obligatoire pour créer une réservation.");
        }
        if (reservation == null) {
            throw new IllegalArgumentException("Les informations de la réservation sont obligatoires.");
        }

        try (Connection connexion = new UtilDB().GetConn()) {
            connexion.setAutoCommit(false);
            try {
                Map<String, List<PlaceSeanceDispo>> placesParSeance =
                        regrouperPlacesParSeance(placesSelectionnees);
                verifierSeancesReservables(connexion, placesParSeance, reservation);
                verrouillerPlaces(connexion, placesParSeance);
                lirePlacesEtVerifierDisponibilite(connexion, placesParSeance);

                creerReservationMere(userEJB, connexion, reservation, placesParSeance);
                Map<String, ReservationDetail> detailsParSeance = creerDetailsReservation(
                        userEJB, connexion, reservation, placesParSeance);
                creerPlacesBloquees(userEJB, connexion, reservation, placesParSeance, detailsParSeance);
                creerVenteBrouillonLiee(userEJB, connexion, reservation);

                connexion.commit();
                return reservation;
            } catch (Exception erreur) {
                try {
                    connexion.rollback();
                } catch (Exception erreurRollback) {
                    erreur.addSuppressed(erreurRollback);
                }
                throw erreur;
            }
        }
    }

    /**
     * Regroupe les places choisies par séance et les ordonne pour permettre
     * des contrôles et des verrouillages cohérents dans les étapes suivantes.
     * L'objet transmis sert ici à vérifier la sélection; sa disponibilité sera
     * de nouveau confirmée en base au moment de créer la réservation.
     */
    private static Map<String, List<PlaceSeanceDispo>> regrouperPlacesParSeance(
            Collection<PlaceSeanceDispo> placesSelectionnees) {
        if (placesSelectionnees == null || placesSelectionnees.isEmpty()) {
            throw new IllegalArgumentException("Sélectionnez au moins une place.");
        }

        Map<String, Map<String, PlaceSeanceDispo>> placesTriees = new TreeMap<>();
        for (PlaceSeanceDispo place : placesSelectionnees) {
            if (place == null) {
                throw new IllegalArgumentException("La sélection contient une place vide.");
            }
            String idSeance = place.getIdseance();
            String idPlace = place.getIdplace();
            if (idSeance == null || idSeance.trim().isEmpty()
                    || idPlace == null || idPlace.trim().isEmpty()) {
                throw new IllegalArgumentException("Chaque place sélectionnée doit être liée à une séance.");
            }
            idSeance = idSeance.trim();
            idPlace = idPlace.trim();
            if (place.getDisponible() != 1) {
                throw new IllegalArgumentException("Une des places sélectionnées n'est plus indiquée comme disponible.");
            }

            Map<String, PlaceSeanceDispo> placesDeLaSeance = placesTriees.computeIfAbsent(
                    idSeance, cle -> new TreeMap<>());
            if (placesDeLaSeance.putIfAbsent(idPlace, place) != null) {
                throw new IllegalArgumentException("La même place a été sélectionnée plusieurs fois pour une séance.");
            }
        }

        Map<String, List<PlaceSeanceDispo>> resultat = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, PlaceSeanceDispo>> entree : placesTriees.entrySet()) {
            resultat.put(entree.getKey(), new ArrayList<>(entree.getValue().values()));
        }
        return resultat;
    }

    /** Vérifie et verrouille les séances, puis fixe une échéance valide au plus tard au début de la première. */
    private static void verifierSeancesReservables(Connection connexion,
            Map<String, List<PlaceSeanceDispo>> placesParSeance, Reservation reservation) throws Exception {
        if (connexion == null) {
            throw new IllegalArgumentException("La connexion est obligatoire pour vérifier les séances.");
        }
        if (reservation == null) {
            throw new IllegalArgumentException("Les informations de la réservation sont obligatoires.");
        }
        if (placesParSeance == null || placesParSeance.isEmpty()) {
            throw new IllegalArgumentException("Aucune séance n'a été sélectionnée.");
        }

        Timestamp debutLePlusTot = null;
        String sql = "SELECT STATUT, DATEHEURE_DEBUT FROM CINE_SEANCE WHERE IDSEANCE = ? FOR UPDATE";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            for (String idSeance : new TreeMap<>(placesParSeance).keySet()) {
                ps.setString(1, idSeance);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new Exception("Séance introuvable : " + idSeance);
                    }
                    String statut = rs.getString("STATUT");
                    if (!ReglesCinema.STATUT_PLANIFIEE.equals(statut)
                            && !ReglesCinema.STATUT_OUVERTE.equals(statut)) {
                        throw new Exception("La séance " + idSeance + " n'est pas ouverte à la réservation.");
                    }
                    Timestamp debutSeance = rs.getTimestamp("DATEHEURE_DEBUT");
                    if (debutSeance == null || !debutSeance.after(new Timestamp(System.currentTimeMillis()))) {
                        throw new Exception("La séance " + idSeance + " a déjà commencé.");
                    }
                    if (debutLePlusTot == null || debutSeance.before(debutLePlusTot)) {
                        debutLePlusTot = debutSeance;
                    }
                }
            }
        }

        Timestamp expiration = reservation.getDate_expiration();
        if (expiration == null) {
            reservation.setDate_expiration(debutLePlusTot);
        } else {
            if (!expiration.after(new Timestamp(System.currentTimeMillis()))) {
                throw new IllegalArgumentException("La date d'expiration doit être dans le futur.");
            }
            if (expiration.after(debutLePlusTot)) {
                throw new IllegalArgumentException(
                        "La date d'expiration ne peut pas dépasser le début de la première séance.");
            }
        }
    }

    /** Verrouille les places sélectionnées dans un ordre stable avant de vérifier leur disponibilité. */
    private static void verrouillerPlaces(Connection connexion,
            Map<String, List<PlaceSeanceDispo>> placesParSeance) throws Exception {
        if (connexion == null) {
            throw new IllegalArgumentException("La connexion est obligatoire pour verrouiller les places.");
        }
        if (placesParSeance == null || placesParSeance.isEmpty()) {
            throw new IllegalArgumentException("Aucune place n'a été sélectionnée.");
        }

        TreeSet<String> identifiantsPlaces = new TreeSet<>();
        for (List<PlaceSeanceDispo> places : placesParSeance.values()) {
            if (places == null || places.isEmpty()) {
                throw new IllegalArgumentException("Une séance sélectionnée ne contient aucune place.");
            }
            for (PlaceSeanceDispo place : places) {
                if (place == null || place.getIdplace() == null || place.getIdplace().trim().isEmpty()) {
                    throw new IllegalArgumentException("Une place sélectionnée n'a pas d'identifiant valide.");
                }
                identifiantsPlaces.add(place.getIdplace().trim());
            }
        }

        String sql = "SELECT IDPLACE FROM CINE_PLACE WHERE IDPLACE = ? FOR UPDATE";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            for (String idPlace : identifiantsPlaces) {
                ps.setString(1, idPlace);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new Exception("Place introuvable : " + idPlace);
                    }
                }
            }
        }
    }

    /** Recharge les données de référence et confirme la disponibilité de chaque place sélectionnée. */
    private static void lirePlacesEtVerifierDisponibilite(Connection connexion,
            Map<String, List<PlaceSeanceDispo>> placesParSeance) throws Exception {
        if (connexion == null) {
            throw new IllegalArgumentException("La connexion est obligatoire pour vérifier les places.");
        }
        if (placesParSeance == null || placesParSeance.isEmpty()) {
            throw new IllegalArgumentException("Aucune place n'a été sélectionnée.");
        }

        String sql = "SELECT p.IDSALLE AS SALLE_PLACE, s.IDSALLE AS SALLE_SEANCE, sa.ACTIF AS SALLE_ACTIVE, "
                + "p.ACTIF AS PLACE_ACTIVE, p.IDCATEGORIEPLACE, cp.ACTIF AS CATEGORIE_ACTIVE, cp.PRIX_BASE, "
                + "CASE WHEN EXISTS (SELECT 1 FROM CINE_RESERVATION_PLACE rp "
                + "WHERE rp.IDSEANCE = s.IDSEANCE AND rp.IDPLACE = p.IDPLACE "
                + "AND rp.STATUT IN (?, ?)) OR EXISTS (SELECT 1 FROM VENTEDETAILS vd "
                + "WHERE vd.IDSEANCE = s.IDSEANCE AND vd.IDPLACE = p.IDPLACE AND vd.STATUT IN (?, ?)) "
                + "THEN 1 ELSE 0 END AS PLACE_OCCUPEE "
                + "FROM CINE_PLACE p JOIN CINE_SEANCE s ON s.IDSEANCE = ? "
                + "JOIN CINE_SALLE sa ON sa.IDSALLE = s.IDSALLE "
                + "JOIN CINE_CATEGORIE_PLACE cp ON cp.IDCATEGORIEPLACE = p.IDCATEGORIEPLACE "
                + "WHERE p.IDPLACE = ?";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            for (Map.Entry<String, List<PlaceSeanceDispo>> entree
                    : new TreeMap<>(placesParSeance).entrySet()) {
                String idSeance = entree.getKey();
                for (PlaceSeanceDispo place : entree.getValue()) {
                    if (place == null || place.getIdplace() == null || place.getIdplace().trim().isEmpty()) {
                        throw new IllegalArgumentException("Une place sélectionnée n'a pas d'identifiant valide.");
                    }
                    if (!idSeance.equals(place.getIdseance())) {
                        throw new IllegalArgumentException("Une place est associée à une séance différente de son groupe.");
                    }

                    ps.setString(1, ReglesCinema.STATUT_BLOQUEE);
                    ps.setString(2, ReglesCinema.STATUT_ACHETEE);
                    ps.setString(3, ReglesCinema.STATUT_BLOQUEE);
                    ps.setString(4, ReglesCinema.STATUT_ACHETEE);
                    ps.setString(5, idSeance);
                    ps.setString(6, place.getIdplace().trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new Exception("La place " + place.getIdplace() + " ou sa séance est introuvable.");
                        }
                        if (!rs.getString("SALLE_PLACE").equals(rs.getString("SALLE_SEANCE"))) {
                            throw new Exception("La place " + place.getIdplace() + " n'appartient pas à la salle de la séance.");
                        }
                        if (rs.getInt("SALLE_ACTIVE") != 1 || rs.getInt("PLACE_ACTIVE") != 1) {
                            throw new Exception("La salle ou la place " + place.getIdplace() + " est inactive.");
                        }
                        if (rs.getInt("CATEGORIE_ACTIVE") != 1) {
                            throw new Exception("La catégorie de la place " + place.getIdplace() + " est inactive.");
                        }
                        if (rs.getInt("PLACE_OCCUPEE") == 1) {
                            throw new Exception("La place " + place.getIdplace() + " est déjà bloquée ou vendue pour cette séance.");
                        }

                        // Les données relues en base deviennent la source du tarif figé dans la réservation.
                        place.setIdsalle(rs.getString("SALLE_PLACE"));
                        place.setIdcategorieplace(rs.getString("IDCATEGORIEPLACE"));
                        place.setPrix_base(rs.getDouble("PRIX_BASE"));
                        place.setDisponible(1);
                    }
                }
            }
        }
    }

    /** Calcule le total depuis les prix relus en base, puis insère la réservation mère dans la transaction reçue. */
    private static Reservation creerReservationMere(UserEJB userEJB, Connection connexion,
            Reservation reservation, Map<String, List<PlaceSeanceDispo>> placesParSeance) throws Exception {
        if (userEJB == null) {
            throw new IllegalArgumentException("Le service utilisateur est obligatoire pour créer la réservation.");
        }
        if (connexion == null) {
            throw new IllegalArgumentException("La connexion est obligatoire pour créer la réservation.");
        }
        if (reservation == null) {
            throw new IllegalArgumentException("Les informations de la réservation sont obligatoires.");
        }
        if (placesParSeance == null || placesParSeance.isEmpty()) {
            throw new IllegalArgumentException("La réservation doit contenir au moins une place.");
        }

        int nombrePlaces = 0;
        BigDecimal montantTotal = BigDecimal.ZERO.setScale(2);
        for (List<PlaceSeanceDispo> places : placesParSeance.values()) {
            if (places == null || places.isEmpty()) {
                throw new IllegalArgumentException("Une séance sélectionnée ne contient aucune place.");
            }
            for (PlaceSeanceDispo place : places) {
                if (place == null) {
                    throw new IllegalArgumentException("La sélection contient une place vide.");
                }
                nombrePlaces = Math.addExact(nombrePlaces, 1);
                montantTotal = montantTotal.add(ReglesCinema.montantDecimal(
                        place.getPrix_base(), "Le tarif de la place " + place.getIdplace() + " est invalide."));
            }
        }
        if (nombrePlaces <= 0) {
            throw new IllegalArgumentException("La réservation doit contenir au moins une place.");
        }

        reservation.setDate_reservation(new Timestamp(System.currentTimeMillis()));
        reservation.setNb_places_total(nombrePlaces);
        reservation.setMontant_total(montantTotal.doubleValue());
        userEJB.createObject(reservation, connexion);
        return reservation;
    }

    /** Crée un détail par séance et renvoie ces détails indexés par identifiant de séance. */
    private static Map<String, ReservationDetail> creerDetailsReservation(UserEJB userEJB,
            Connection connexion, Reservation reservation,
            Map<String, List<PlaceSeanceDispo>> placesParSeance) throws Exception {
        if (userEJB == null || connexion == null || reservation == null) {
            throw new IllegalArgumentException("Le service, la connexion et la réservation sont obligatoires.");
        }
        if (ReglesCinema.estVide(reservation.getIdreservation())) {
            throw new IllegalArgumentException("La réservation doit être créée avant ses détails.");
        }
        if (placesParSeance == null || placesParSeance.isEmpty()) {
            throw new IllegalArgumentException("Aucun détail ne peut être créé sans séance ni place.");
        }

        Map<String, ReservationDetail> detailsParSeance = new LinkedHashMap<>();
        for (Map.Entry<String, List<PlaceSeanceDispo>> entree
                : new TreeMap<>(placesParSeance).entrySet()) {
            String idSeance = entree.getKey();
            List<PlaceSeanceDispo> places = entree.getValue();
            if (ReglesCinema.estVide(idSeance) || places == null || places.isEmpty()) {
                throw new IllegalArgumentException("Chaque détail doit correspondre à une séance avec au moins une place.");
            }

            int nombrePlaces = 0;
            BigDecimal montantLigne = BigDecimal.ZERO.setScale(2);
            for (PlaceSeanceDispo place : places) {
                if (place == null) {
                    throw new IllegalArgumentException("La sélection contient une place vide.");
                }
                nombrePlaces = Math.addExact(nombrePlaces, 1);
                montantLigne = montantLigne.add(ReglesCinema.montantDecimal(
                        place.getPrix_base(), "Le tarif de la place " + place.getIdplace() + " est invalide."));
            }

            ReservationDetail detail = new ReservationDetail();
            detail.setIdreservation(reservation.getIdreservation());
            detail.setIdseance(idSeance);
            detail.setNb_places(nombrePlaces);
            detail.setMontant_ligne(montantLigne.doubleValue());
            Object detailCree = userEJB.createObject(detail, connexion);
            if (!(detailCree instanceof ReservationDetail)) {
                throw new Exception("Le framework n'a pas retourné le détail créé pour la séance " + idSeance + ".");
            }
            detailsParSeance.put(idSeance, (ReservationDetail) detailCree);
        }
        return detailsParSeance;
    }

    /** Crée une ligne de réservation bloquée pour chaque place, rattachée au détail de sa séance. */
    private static void creerPlacesBloquees(UserEJB userEJB, Connection connexion,
            Reservation reservation, Map<String, List<PlaceSeanceDispo>> placesParSeance,
            Map<String, ReservationDetail> detailsParSeance) throws Exception {
        if (userEJB == null || connexion == null || reservation == null) {
            throw new IllegalArgumentException("Le service, la connexion et la réservation sont obligatoires.");
        }
        if (ReglesCinema.estVide(reservation.getIdreservation()) || reservation.getDate_expiration() == null) {
            throw new IllegalArgumentException("La réservation doit avoir un identifiant et une date d'expiration.");
        }
        if (placesParSeance == null || placesParSeance.isEmpty()
                || detailsParSeance == null || detailsParSeance.isEmpty()) {
            throw new IllegalArgumentException("Les places et leurs détails doivent être préparés avant leur blocage.");
        }

        Timestamp dateBlocage = new Timestamp(System.currentTimeMillis());
        for (Map.Entry<String, List<PlaceSeanceDispo>> entree
                : new TreeMap<>(placesParSeance).entrySet()) {
            String idSeance = entree.getKey();
            ReservationDetail detail = detailsParSeance.get(idSeance);
            if (detail == null || ReglesCinema.estVide(detail.getIdreservation_detail())) {
                throw new IllegalArgumentException("Aucun détail enregistré pour la séance " + idSeance + ".");
            }
            if (!reservation.getIdreservation().equals(detail.getIdreservation())
                    || !idSeance.equals(detail.getIdseance())) {
                throw new IllegalArgumentException("Le détail ne correspond pas à la réservation et à la séance.");
            }

            for (PlaceSeanceDispo place : entree.getValue()) {
                if (place == null || ReglesCinema.estVide(place.getIdplace())
                        || ReglesCinema.estVide(place.getIdcategorieplace())) {
                    throw new IllegalArgumentException("La place ou sa catégorie n'a pas été chargée correctement.");
                }
                ReservationPlace reservationPlace = new ReservationPlace();
                reservationPlace.setIdreservation_detail(detail.getIdreservation_detail());
                reservationPlace.setIdseance(idSeance);
                reservationPlace.setIdplace(place.getIdplace());
                reservationPlace.setIdcategorieplace(place.getIdcategorieplace());
                reservationPlace.setPrix_unitaire(ReglesCinema.montantDecimal(
                        place.getPrix_base(), "Le tarif de la place " + place.getIdplace() + " est invalide.").doubleValue());
                reservationPlace.setDate_blocage(dateBlocage);
                reservationPlace.setDate_expiration(reservation.getDate_expiration());
                userEJB.createObject(reservationPlace, connexion);
            }
        }
    }

    /** Crée la vente brouillon liée à la réservation, sans caisse ni facture à ce stade. */
    private static Vente creerVenteBrouillonLiee(UserEJB userEJB, Connection connexion,
            Reservation reservation) throws Exception {
        if (userEJB == null || connexion == null || reservation == null) {
            throw new IllegalArgumentException("Le service, la connexion et la réservation sont obligatoires.");
        }
        if (ReglesCinema.estVide(reservation.getIdreservation())
                || ReglesCinema.estVide(reservation.getIdclient())) {
            throw new IllegalArgumentException("La réservation doit avoir un identifiant et un client.");
        }

        Vente vente = new Vente();
        vente.setDaty(new Timestamp(System.currentTimeMillis()));
        vente.setType_vente(ReglesCinema.TYPE_VENTE_RESERVATION);
        vente.setIdclient(reservation.getIdclient());
        vente.setIdreservation(reservation.getIdreservation());
        vente.setMontant_total(ReglesCinema.montantDecimal(
                reservation.getMontant_total(), "Le montant de la réservation est invalide.").doubleValue());
        Object venteCreee = userEJB.createObject(vente, connexion);
        if (!(venteCreee instanceof Vente)) {
            throw new Exception("Le framework n'a pas retourné la vente brouillon créée.");
        }
        return (Vente) venteCreee;
    }

}
