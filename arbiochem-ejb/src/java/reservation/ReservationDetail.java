package reservation;

import bean.ClassMAPTable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReservationDetail extends ClassMAPTable {
    private String idreservation_detail;
    private String idreservation;
    private String idseance;
    private int nb_places;
    private double montant_ligne;
    private String statut = "EN_ATTENTE";
    public ReservationDetail() { setNomTable("CINE_RESERVATION_DETAIL"); }
    @Override public String getTuppleID() { return idreservation_detail; }
    @Override public String getAttributIDName() { return "idreservation_detail"; }
    @Override public void construirePK(Connection c) throws Exception { preparePk("RSD", "getSeqCineReservationDetail"); setIdreservation_detail(makePK(c)); }

    @Override
    public void controler(Connection connexion) throws Exception {
        if (connexion == null) {
            throw new IllegalArgumentException("La connexion est obligatoire pour contrôler le détail de réservation.");
        }
        if (idreservation == null || idreservation.trim().isEmpty()) {
            throw new IllegalArgumentException("La réservation mère est obligatoire.");
        }
        if (idseance == null || idseance.trim().isEmpty()) {
            throw new IllegalArgumentException("La séance est obligatoire.");
        }
        if (nb_places <= 0) {
            throw new IllegalArgumentException("Le détail doit contenir au moins une place.");
        }
        if (montant_ligne < 0 || Double.isNaN(montant_ligne) || Double.isInfinite(montant_ligne)) {
            throw new IllegalArgumentException("Le montant de la ligne est invalide.");
        }
        if (!"EN_ATTENTE".equals(statut)) {
            throw new IllegalArgumentException("Un détail créé doit avoir le statut EN_ATTENTE.");
        }

        String sql = "SELECT r.STATUT AS STATUT_RESERVATION, "
                + "CASE WHEN r.DATE_EXPIRATION > SYSDATE THEN 1 ELSE 0 END AS RESERVATION_ACTIVE, "
                + "s.STATUT AS STATUT_SEANCE, "
                + "CASE WHEN s.DATEHEURE_DEBUT > SYSDATE THEN 1 ELSE 0 END AS SEANCE_FUTURE "
                + "FROM CINE_RESERVATION r CROSS JOIN CINE_SEANCE s "
                + "WHERE r.IDRESERVATION = ? AND s.IDSEANCE = ?";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idreservation);
            ps.setString(2, idseance);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("La réservation mère ou la séance est introuvable.");
                }
                if (!"EN_ATTENTE".equals(rs.getString("STATUT_RESERVATION"))) {
                    throw new Exception("Le détail ne peut être ajouté qu'à une réservation en attente.");
                }
                if (rs.getInt("RESERVATION_ACTIVE") != 1) {
                    throw new Exception("La réservation a atteint sa date d'expiration.");
                }
                String statutSeance = rs.getString("STATUT_SEANCE");
                if (!"PLANIFIEE".equals(statutSeance) && !"OUVERTE".equals(statutSeance)) {
                    throw new Exception("La séance n'est pas disponible pour une réservation.");
                }
                if (rs.getInt("SEANCE_FUTURE") != 1) {
                    throw new Exception("Une réservation ne peut pas concerner une séance déjà commencée.");
                }
            }
        }
    }

    public String getIdreservation_detail() { return idreservation_detail; }
    public void setIdreservation_detail(String v) { idreservation_detail = v; }
    public String getIdreservation() { return idreservation; }
    public void setIdreservation(String v) { idreservation = v; }
    public String getIdseance() { return idseance; }
    public void setIdseance(String v) { idseance = v; }
    public int getNb_places() { return nb_places; }
    public void setNb_places(int v) { nb_places = v; }
    public double getMontant_ligne() { return montant_ligne; }
    public void setMontant_ligne(double v) { montant_ligne = v; }
    public String getStatut() { return statut; }
    public void setStatut(String v) { statut = v; }
}
