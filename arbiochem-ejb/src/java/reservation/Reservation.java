package reservation;

import bean.ClassMAPTable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class Reservation extends ClassMAPTable {

    private String idreservation;
    private String numero_reservation;
    private String idclient;
    private String idmodepaiement_prevu;
    private Timestamp date_reservation;
    private Timestamp date_expiration;
    private String statut = "EN_ATTENTE";
    private int nb_places_total;
    private double montant_total;
    private String remarque;

    public Reservation() { setNomTable("CINE_RESERVATION"); }
    @Override public String getTuppleID() { return idreservation; }
    @Override public String getAttributIDName() { return "idreservation"; }
    @Override public void construirePK(Connection c) throws Exception { preparePk("RES", "getSeqCineReservation"); setIdreservation(makePK(c)); }

    @Override
    public void controler(Connection connexion) throws Exception {
        if (connexion == null) {
            throw new IllegalArgumentException("La connexion est obligatoire pour contrôler la réservation.");
        }
        if (numero_reservation == null || numero_reservation.trim().isEmpty()) {
            throw new IllegalArgumentException("Le numéro de réservation est obligatoire.");
        }
        if (numero_reservation.length() > 30) {
            throw new IllegalArgumentException("Le numéro de réservation ne peut pas dépasser 30 caractères.");
        }
        if (idclient == null || idclient.trim().isEmpty()) {
            throw new IllegalArgumentException("Le client est obligatoire pour une réservation.");
        }
        if (idmodepaiement_prevu == null || idmodepaiement_prevu.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mode de paiement prévu est obligatoire.");
        }
        verifierReferenceActive(connexion, "CLIENT", "IDCLIENT", idclient, "client");
        verifierReferenceActive(connexion, "MODEPAIEMENT", "IDMODEPAIEMENT",
                idmodepaiement_prevu, "mode de paiement");

        Timestamp maintenant = new Timestamp(System.currentTimeMillis());
        if (date_reservation == null) {
            throw new IllegalArgumentException("La date de réservation est obligatoire.");
        }
        if (date_expiration == null || !date_expiration.after(maintenant)) {
            throw new IllegalArgumentException("La date d'expiration doit être dans le futur.");
        }
        if (date_expiration.before(date_reservation)) {
            throw new IllegalArgumentException("L'expiration ne peut pas précéder la réservation.");
        }
        if (!"EN_ATTENTE".equals(statut)) {
            throw new IllegalArgumentException("Une nouvelle réservation doit avoir le statut EN_ATTENTE.");
        }
        if (nb_places_total <= 0) {
            throw new IllegalArgumentException("Une réservation doit contenir au moins une place.");
        }
        if (montant_total < 0 || Double.isNaN(montant_total) || Double.isInfinite(montant_total)) {
            throw new IllegalArgumentException("Le montant total de la réservation est invalide.");
        }
    }

    private static void verifierReferenceActive(Connection connexion, String table,
            String colonneId, String id, String libelle) throws Exception {
        String sql = "SELECT ACTIF FROM " + table + " WHERE " + colonneId + " = ?";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Le " + libelle + " sélectionné est introuvable.");
                if (rs.getInt(1) != 1) throw new Exception("Le " + libelle + " sélectionné est inactif.");
            }
        }
    }

    public String getIdreservation() { return idreservation; }
    public void setIdreservation(String v) { idreservation = v; }
    public String getNumero_reservation() { return numero_reservation; }
    public void setNumero_reservation(String v) { numero_reservation = v; }
    public String getIdclient() { return idclient; }
    public void setIdclient(String v) { idclient = v; }
    public String getIdmodepaiement_prevu() { return idmodepaiement_prevu; }
    public void setIdmodepaiement_prevu(String v) { idmodepaiement_prevu = v; }
    public Timestamp getDate_reservation() { return date_reservation; }
    public void setDate_reservation(Timestamp v) { date_reservation = v; }
    public Timestamp getDate_expiration() { return date_expiration; }
    public void setDate_expiration(Timestamp v) { date_expiration = v; }
    public String getStatut() { return statut; }
    public void setStatut(String v) { statut = v; }
    public int getNb_places_total() { return nb_places_total; }
    public void setNb_places_total(int v) { nb_places_total = v; }
    public double getMontant_total() { return montant_total; }
    public void setMontant_total(double v) { montant_total = v; }
    public String getRemarque() { return remarque; }
    public void setRemarque(String v) { remarque = v; }
}
