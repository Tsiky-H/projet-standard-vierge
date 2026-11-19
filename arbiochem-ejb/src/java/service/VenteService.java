package service;

import cinema.ReglesCinema;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import user.UserEJB;
import utilitaire.UtilDB;
import vente.Vente;
import vente.VenteDetail;

/** Création transactionnelle d'une vente directe de billets. */
public final class VenteService {

    private VenteService() { }

    public static Vente creerVenteDirecte(UserEJB utilisateur, String idClient, String idSeance,
            Collection<String> placesSelectionnees) throws Exception {
        if (utilisateur == null) throw new IllegalArgumentException("La session utilisateur est obligatoire.");
        if (ReglesCinema.estVide(idClient)) throw new IllegalArgumentException("Le client est obligatoire.");
        if (ReglesCinema.estVide(idSeance)) throw new IllegalArgumentException("La séance est obligatoire.");
        if (placesSelectionnees == null || placesSelectionnees.isEmpty()) {
            throw new IllegalArgumentException("Sélectionnez au moins une place.");
        }

        TreeSet<String> idsPlaces = new TreeSet<String>();
        for (String id : placesSelectionnees) {
            if (ReglesCinema.estVide(id) || !idsPlaces.add(id.trim())) {
                throw new IllegalArgumentException("La sélection contient une place vide ou répétée.");
            }
        }

        try (Connection connexion = new UtilDB().GetConn()) {
            connexion.setAutoCommit(false);
            try {
                SeanceInfo seance = verrouillerSeance(connexion, idSeance.trim());
                Timestamp maintenant = new Timestamp(System.currentTimeMillis());
                if (!ReglesCinema.STATUT_PLANIFIEE.equals(seance.statut)
                        && !ReglesCinema.STATUT_OUVERTE.equals(seance.statut)) {
                    throw new IllegalArgumentException("Cette séance n'est plus ouverte à la vente.");
                }
                if (seance.fin == null || !seance.fin.after(maintenant)) {
                    throw new IllegalArgumentException("La séance est terminée; aucune vente n'est possible.");
                }

                List<LignePlace> lignes = new ArrayList<LignePlace>();
                BigDecimal total = BigDecimal.ZERO;
                for (String idPlace : idsPlaces) {
                    verrouillerPlace(connexion, idPlace);
                    LignePlace ligne = lireEtVerifierPlace(connexion, idSeance.trim(), idPlace, seance);
                    lignes.add(ligne);
                    total = total.add(ligne.prix);
                }
                String idCaisse = lirePremiereCaisseActive(connexion);

                Vente vente = new Vente();
                vente.setDaty(maintenant);
                vente.setType_vente(ReglesCinema.TYPE_VENTE_DIRECTE);
                vente.setStatut(ReglesCinema.STATUT_BROUILLON);
                vente.setIdclient(idClient.trim());
                vente.setIdcaisse(idCaisse);
                vente.setMontant_total(total.doubleValue());
                Object creee = utilisateur.createObject(vente, connexion);
                if (!(creee instanceof Vente)) throw new Exception("La vente n'a pas pu être créée.");
                vente = (Vente) creee;

                for (LignePlace place : lignes) {
                    VenteDetail detail = new VenteDetail();
                    detail.setIdvente(vente.getIdvente());
                    detail.setIdseance(idSeance.trim());
                    detail.setIdplace(place.idPlace);
                    detail.setIdcategorieplace(place.idCategorie);
                    String designation = seance.titre + " — " + seance.debut + " — " + place.libelle;
                    detail.setDesignation(designation.length() <= 250 ? designation : designation.substring(0, 250));
                    detail.setQuantite(1);
                    detail.setPrix_unitaire(place.prix.doubleValue());
                    detail.setMontant_ligne(place.prix.doubleValue());
                    detail.setStatut(ReglesCinema.STATUT_BLOQUEE);
                    utilisateur.createObject(detail, connexion);
                }
                connexion.commit();
                return vente;
            } catch (Exception e) {
                try { connexion.rollback(); } catch (Exception rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
    }

    private static SeanceInfo verrouillerSeance(Connection c, String id) throws Exception {
        String sql = "SELECT s.STATUT, s.DATEHEURE_DEBUT, s.DATEHEURE_FIN, f.TITRE, "
                + "f.ACTIF AS FILM_ACTIF, sa.ACTIF AS SALLE_ACTIVE "
                + "FROM CINE_SEANCE s JOIN CINE_FILM f ON f.IDFILM = s.IDFILM "
                + "JOIN CINE_SALLE sa ON sa.IDSALLE = s.IDSALLE "
                + "WHERE s.IDSEANCE = ? FOR UPDATE OF s.STATUT";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("La séance sélectionnée est introuvable.");
                if (rs.getInt("FILM_ACTIF") != 1 || rs.getInt("SALLE_ACTIVE") != 1) {
                    throw new IllegalArgumentException("Le film ou la salle de cette séance est inactif.");
                }
                SeanceInfo info = new SeanceInfo();
                info.statut = rs.getString("STATUT");
                info.debut = rs.getTimestamp("DATEHEURE_DEBUT");
                info.fin = rs.getTimestamp("DATEHEURE_FIN");
                info.titre = rs.getString("TITRE");
                return info;
            }
        }
    }

    private static void verrouillerPlace(Connection c, String idPlace) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT IDPLACE FROM CINE_PLACE WHERE IDPLACE = ? FOR UPDATE")) {
            ps.setString(1, idPlace);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Une place sélectionnée est introuvable.");
            }
        }
    }

    private static LignePlace lireEtVerifierPlace(Connection c, String idSeance, String idPlace,
            SeanceInfo seance) throws Exception {
        String sql = "SELECT p.IDCATEGORIEPLACE, p.NUMERO, p.RANGEE, p.ACTIF, cp.LIBELLE, cp.PRIX_BASE, cp.ACTIF AS CAT_ACTIVE "
                + "FROM CINE_SEANCE s JOIN CINE_PLACE p ON p.IDSALLE = s.IDSALLE "
                + "JOIN CINE_CATEGORIE_PLACE cp ON cp.IDCATEGORIEPLACE = p.IDCATEGORIEPLACE "
                + "WHERE s.IDSEANCE = ? AND p.IDPLACE = ?";
        LignePlace ligne = new LignePlace();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, idSeance);
            ps.setString(2, idPlace);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("La place ne fait pas partie de cette séance.");
                if (rs.getInt("ACTIF") != 1 || rs.getInt("CAT_ACTIVE") != 1) {
                    throw new IllegalArgumentException("La place sélectionnée ou sa catégorie est inactive.");
                }
                ligne.idPlace = idPlace;
                ligne.idCategorie = rs.getString("IDCATEGORIEPLACE");
                ligne.prix = rs.getBigDecimal("PRIX_BASE");
                if (ligne.prix == null || ligne.prix.signum() < 0) throw new IllegalArgumentException("Prix de place invalide.");
                ligne.prix = ligne.prix.setScale(2, java.math.RoundingMode.HALF_UP);
                ligne.libelle = (rs.getString("RANGEE") == null ? "" : rs.getString("RANGEE"))
                        + rs.getString("NUMERO") + " — " + rs.getString("LIBELLE");
            }
        }
        String sqlOccupee = "SELECT 1 FROM CINE_RESERVATION_PLACE WHERE IDSEANCE = ? AND IDPLACE = ? "
                + "AND STATUT IN ('BLOQUEE','ACHETEE') AND ROWNUM = 1";
        try (PreparedStatement ps = c.prepareStatement(sqlOccupee)) {
            ps.setString(1, idSeance); ps.setString(2, idPlace);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) throw new IllegalArgumentException("Une des places sélectionnées n'est plus disponible.");
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM VENTEDETAILS WHERE IDSEANCE = ? AND IDPLACE = ? AND STATUT IN ('BLOQUEE','ACHETEE') AND ROWNUM = 1")) {
            ps.setString(1, idSeance); ps.setString(2, idPlace);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) throw new IllegalArgumentException("Une des places sélectionnées n'est plus disponible.");
            }
        }
        return ligne;
    }

    private static String lirePremiereCaisseActive(Connection c) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT IDCAISSE FROM CAISSE WHERE ACTIF = 1 ORDER BY IDCAISSE");
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) throw new IllegalArgumentException("Aucune caisse active n'est configurée.");
            return rs.getString(1);
        }
    }

    private static final class SeanceInfo {
        String statut;
        Timestamp debut;
        Timestamp fin;
        String titre;
    }

    private static final class LignePlace {
        String idPlace;
        String idCategorie;
        String libelle;
        BigDecimal prix;
    }
}
