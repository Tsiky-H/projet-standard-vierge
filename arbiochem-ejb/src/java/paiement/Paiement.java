package paiement;

import bean.ClassMAPTable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Paiement extends ClassMAPTable {
    private String idpaiement;
    private String numero_paiement;
    private Timestamp daty;
    private String idmodepaiement;
    private String statut = "EN_ATTENTE";
    private double montant;
    private String reference_externe;
    private String idreservation;
    private String idvente;
    public Paiement() { setNomTable("CINE_PAIEMENT"); }
    @Override public String getTuppleID() { return idpaiement; }
    @Override public String getAttributIDName() { return "idpaiement"; }
    @Override public void construirePK(Connection c) throws Exception { preparePk("PAY", "getSeqCinePaiement"); setIdpaiement(makePK(c)); }

    @Override
    public void controler(Connection connexion) throws Exception {
        if (connexion == null) {
            throw new IllegalArgumentException("La connexion est obligatoire pour contrôler le paiement.");
        }
        if (numero_paiement == null || numero_paiement.trim().isEmpty() || numero_paiement.length() > 30) {
            throw new IllegalArgumentException("Le numéro de paiement est obligatoire et limité à 30 caractères.");
        }
        if (daty == null) {
            throw new IllegalArgumentException("La date du paiement est obligatoire.");
        }
        if (idmodepaiement == null || idmodepaiement.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mode de paiement est obligatoire.");
        }
        if (idvente == null || idvente.trim().isEmpty()) {
            throw new IllegalArgumentException("Le paiement doit être rattaché à une vente.");
        }
        if (idreservation != null && !idreservation.trim().isEmpty()) {
            throw new IllegalArgumentException("Le paiement référence la vente, pas directement la réservation.");
        }
        if (montant <= 0 || Double.isNaN(montant) || Double.isInfinite(montant)) {
            throw new IllegalArgumentException("Le montant du paiement est invalide.");
        }
        if (reference_externe != null && reference_externe.length() > 120) {
            throw new IllegalArgumentException("La référence externe ne peut pas dépasser 120 caractères.");
        }
        if (!"EN_ATTENTE".equals(statut) && !"VALIDE".equals(statut)
                && !"REFUSE".equals(statut) && !"ANNULE".equals(statut)) {
            throw new IllegalArgumentException("Le statut du paiement est invalide.");
        }

        verifierModePaiementActif(connexion);
        verifierVenteEtMontant(connexion);
    }

    private void verifierModePaiementActif(Connection connexion) throws Exception {
        String sql = "SELECT ACTIF FROM MODEPAIEMENT WHERE IDMODEPAIEMENT = ?";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idmodepaiement);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Le mode de paiement sélectionné est introuvable.");
                if (rs.getInt(1) != 1) throw new Exception("Le mode de paiement sélectionné est inactif.");
            }
        }
    }

    private void verifierVenteEtMontant(Connection connexion) throws Exception {
        String sql = "SELECT v.STATUT, v.MONTANT_TOTAL, v.IDRESERVATION, "
                + "r.STATUT AS STATUT_RESERVATION, r.DATE_EXPIRATION "
                + "FROM VENTE v LEFT JOIN CINE_RESERVATION r ON r.IDRESERVATION = v.IDRESERVATION "
                + "WHERE v.IDVENTE = ?";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idvente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("La vente associée au paiement est introuvable.");
                if (!"BROUILLON".equals(rs.getString("STATUT"))) {
                    throw new Exception("Le paiement ne peut être enregistré que pour une vente brouillon.");
                }
                if (rs.getString("IDRESERVATION") != null) {
                    if (!"EN_ATTENTE".equals(rs.getString("STATUT_RESERVATION"))) {
                        throw new Exception("La réservation associée à la vente n'est plus payable.");
                    }
                    Timestamp dateExpiration = rs.getTimestamp("DATE_EXPIRATION");
                    if (dateExpiration == null || daty.after(dateExpiration)) {
                        throw new Exception("Le paiement de la réservation est tardif.");
                    }
                }
                BigDecimal montantVente = rs.getBigDecimal("MONTANT_TOTAL");
                BigDecimal montantPaiement = BigDecimal.valueOf(montant).setScale(2, RoundingMode.HALF_UP);
                if (montantVente == null || montantPaiement.compareTo(montantVente.setScale(2, RoundingMode.HALF_UP)) != 0) {
                    throw new Exception("Le paiement doit couvrir le montant total de la vente.");
                }
            }
        }
    }

    public String getIdpaiement() { return idpaiement; }
    public void setIdpaiement(String v) { idpaiement = v; }
    public String getNumero_paiement() { return numero_paiement; }
    public void setNumero_paiement(String v) { numero_paiement = v; }
    public Timestamp getDaty() { return daty; }
    public void setDaty(Timestamp v) { daty = v; }
    public String getIdmodepaiement() { return idmodepaiement; }
    public void setIdmodepaiement(String v) { idmodepaiement = v; }
    public String getStatut() { return statut; }
    public void setStatut(String v) { statut = v; }
    public double getMontant() { return montant; }
    public void setMontant(double v) { montant = v; }
    public String getReference_externe() { return reference_externe; }
    public void setReference_externe(String v) { reference_externe = v; }
    public String getIdreservation() { return idreservation; }
    public void setIdreservation(String v) { idreservation = v; }
    public String getIdvente() { return idvente; }
    public void setIdvente(String v) { idvente = v; }
}
