package reservation;

import bean.ClassMAPTable;
import cinema.ReglesCinema;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class ReservationPlace extends ClassMAPTable {
    private String idreservation_place;
    private String idreservation_detail;
    private String idseance;
    private String idplace;
    private String idcategorieplace;
    private double prix_unitaire;
    // private String statut = ReglesCinema.STATUT_BLOQUEE;
    private String statut;
    private Timestamp date_blocage;
    private Timestamp date_expiration;
    private Timestamp date_validation;
    public ReservationPlace() { setNomTable("CINE_RESERVATION_PLACE"); }
    @Override public String getTuppleID() { return idreservation_place; }
    @Override public String getAttributIDName() { return "idreservation_place"; }
    @Override public void construirePK(Connection c) throws Exception { preparePk("RSP", "getSeqCineReservationPlace"); setIdreservation_place(makePK(c)); }

    @Override
    public void controler(Connection connexion) throws Exception {
        if (connexion == null) {
            throw new IllegalArgumentException("La connexion est obligatoire pour contrôler la place réservée.");
        }
        if (idreservation_detail == null || idreservation_detail.trim().isEmpty()
                || idseance == null || idseance.trim().isEmpty()
                || idplace == null || idplace.trim().isEmpty()
                || idcategorieplace == null || idcategorieplace.trim().isEmpty()) {
            throw new IllegalArgumentException("Le détail, la séance, la place et sa catégorie sont obligatoires.");
        }
        BigDecimal prix = ReglesCinema.montantDecimal(
                prix_unitaire, "Le prix unitaire de la place est invalide.");
        if (prix.signum() < 0) {
            throw new IllegalArgumentException("Le prix unitaire de la place est invalide.");
        }
        if (!ReglesCinema.STATUT_BLOQUEE.equals(statut)) {
            throw new IllegalArgumentException("Une place créée dans une réservation doit avoir le statut BLOQUEE.");
        }
        if (date_blocage == null || date_expiration == null
                || date_expiration.before(date_blocage)
                || !date_expiration.after(new Timestamp(System.currentTimeMillis()))) {
            throw new IllegalArgumentException("Les dates de blocage et d'expiration sont invalides.");
        }
        if (date_validation != null) {
            throw new IllegalArgumentException("Une place bloquée ne peut pas avoir de date de validation.");
        }

        String sql = "SELECT d.IDSEANCE, d.STATUT AS STATUT_DETAIL, "
                + "r.STATUT AS STATUT_RESERVATION, r.DATE_EXPIRATION AS EXPIRATION_RESERVATION, "
                + "p.IDSALLE AS SALLE_PLACE, p.ACTIF AS PLACE_ACTIVE, s.IDSALLE AS SALLE_SEANCE, "
                + "p.IDCATEGORIEPLACE AS CATEGORIE_PLACE, cp.ACTIF AS CATEGORIE_ACTIVE, "
                + "s.STATUT AS STATUT_SEANCE, s.DATEHEURE_DEBUT "
                + "FROM CINE_RESERVATION_DETAIL d "
                + "JOIN CINE_RESERVATION r ON r.IDRESERVATION = d.IDRESERVATION "
                + "JOIN CINE_PLACE p ON p.IDPLACE = ? "
                + "JOIN CINE_SEANCE s ON s.IDSEANCE = ? "
                + "JOIN CINE_CATEGORIE_PLACE cp ON cp.IDCATEGORIEPLACE = ? "
                + "WHERE d.IDRESERVATION_DETAIL = ? FOR UPDATE OF p.IDPLACE";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idplace);
            ps.setString(2, idseance);
            ps.setString(3, idcategorieplace);
            ps.setString(4, idreservation_detail);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("Le détail, la séance, la place ou la catégorie est introuvable.");
                }
                if (!idseance.equals(rs.getString("IDSEANCE"))) {
                    throw new Exception("La séance de la place ne correspond pas à celle du détail.");
                }
                if (!ReglesCinema.STATUT_EN_ATTENTE.equals(rs.getString("STATUT_DETAIL"))) {
                    throw new Exception("Une place ne peut être ajoutée qu'à un détail en attente.");
                }
                if (rs.getInt("PLACE_ACTIVE") != 1) {
                    throw new Exception("La place sélectionnée est inactive.");
                }
                if (!rs.getString("SALLE_PLACE").equals(rs.getString("SALLE_SEANCE"))) {
                    throw new Exception("La place n'appartient pas à la salle de cette séance.");
                }
                if (!idcategorieplace.equals(rs.getString("CATEGORIE_PLACE"))) {
                    throw new Exception("La catégorie ne correspond pas à celle de la place.");
                }
                if (rs.getInt("CATEGORIE_ACTIVE") != 1) {
                    throw new Exception("La catégorie de la place est inactive.");
                }
                if (!ReglesCinema.STATUT_EN_ATTENTE.equals(rs.getString("STATUT_RESERVATION"))) {
                    throw new Exception("La réservation n'est plus en attente.");
                }
                Timestamp expirationReservation = rs.getTimestamp("EXPIRATION_RESERVATION");
                if (expirationReservation == null || !expirationReservation.after(date_blocage)) {
                    throw new Exception("La réservation a expiré ou sa date de blocage est incohérente.");
                }
                if (date_expiration.getTime() / 1000 != expirationReservation.getTime() / 1000) {
                    throw new Exception("L'expiration de la place doit correspondre à celle de la réservation.");
                }
                String statutSeance = rs.getString("STATUT_SEANCE");
                if ((!ReglesCinema.STATUT_PLANIFIEE.equals(statutSeance)
                        && !ReglesCinema.STATUT_OUVERTE.equals(statutSeance))
                        || rs.getTimestamp("DATEHEURE_DEBUT") == null
                        || !rs.getTimestamp("DATEHEURE_DEBUT").after(new Timestamp(System.currentTimeMillis()))) {
                    throw new Exception("La séance n'est plus disponible à la réservation.");
                }
            }
        }
        verifierPlaceDisponible(connexion);
    }

    private void verifierPlaceDisponible(Connection connexion) throws Exception {
        String[] requetes = {
            "SELECT 1 FROM CINE_RESERVATION_PLACE WHERE IDSEANCE = ? AND IDPLACE = ? "
                + "AND STATUT IN ('" + ReglesCinema.STATUT_BLOQUEE + "', '" + ReglesCinema.STATUT_ACHETEE + "') AND ROWNUM = 1",
            "SELECT 1 FROM VENTEDETAILS WHERE IDSEANCE = ? AND IDPLACE = ? "
                + "AND STATUT IN ('" + ReglesCinema.STATUT_BLOQUEE + "', '" + ReglesCinema.STATUT_ACHETEE + "') AND ROWNUM = 1"
        };
        for (String sql : requetes) {
            try (PreparedStatement ps = connexion.prepareStatement(sql)) {
                ps.setString(1, idseance);
                ps.setString(2, idplace);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new Exception("La place est déjà bloquée ou vendue pour cette séance.");
                    }
                }
            }
        }
    }

    public String getIdreservation_place() { return idreservation_place; }
    public void setIdreservation_place(String v) { idreservation_place = v; }
    public String getIdreservation_detail() { return idreservation_detail; }
    public void setIdreservation_detail(String v) { idreservation_detail = v; }
    public String getIdseance() { return idseance; }
    public void setIdseance(String v) { idseance = v; }
    public String getIdplace() { return idplace; }
    public void setIdplace(String v) { idplace = v; }
    public String getIdcategorieplace() { return idcategorieplace; }
    public void setIdcategorieplace(String v) { idcategorieplace = v; }
    public double getPrix_unitaire() { return prix_unitaire; }
    public void setPrix_unitaire(double v) { prix_unitaire = v; }
    public String getStatut() { return statut; }
    public void setStatut(String v) { statut = v; }
    public Timestamp getDate_blocage() { return date_blocage; }
    public void setDate_blocage(Timestamp v) { date_blocage = v; }
    public Timestamp getDate_expiration() { return date_expiration; }
    public void setDate_expiration(Timestamp v) { date_expiration = v; }
    public Timestamp getDate_validation() { return date_validation; }
    public void setDate_validation(Timestamp v) { date_validation = v; }
}
