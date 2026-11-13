package vente;

import bean.ClassMAPTable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Vente extends ClassMAPTable {
    private String idvente;
    private Date daty;
    private String type_vente;
    private String statut = "BROUILLON";
    private String idclient;
    private String idcaisse;
    private String idreservation;
    private String idfactureclient;
    private double montant_total;
    public Vente() { setNomTable("VENTE"); }
    @Override public String getTuppleID() { return idvente; }
    @Override public String getAttributIDName() { return "idvente"; }
    @Override public void construirePK(Connection c) throws Exception { preparePk("VEN", "getSeqVente"); setIdvente(makePK(c)); }

    @Override
    public void controler(Connection connexion) throws Exception {
        if (connexion == null) {
            throw new IllegalArgumentException("La connexion est obligatoire pour contrôler la vente.");
        }
        if (daty == null) {
            throw new IllegalArgumentException("La date de vente est obligatoire.");
        }
        if (!"DIRECTE".equals(type_vente) && !"RESERVATION".equals(type_vente)) {
            throw new IllegalArgumentException("Le type de vente doit être DIRECTE ou RESERVATION.");
        }
        if (!"BROUILLON".equals(statut)) {
            throw new IllegalArgumentException("Une nouvelle vente doit avoir le statut BROUILLON.");
        }
        boolean caisseAbsente = idcaisse == null || idcaisse.trim().isEmpty();
        if (caisseAbsente && !("RESERVATION".equals(type_vente) && "BROUILLON".equals(statut))) {
            throw new IllegalArgumentException("Seule une vente brouillon de réservation peut ne pas encore avoir de caisse.");
        }
        if (montant_total < 0 || Double.isNaN(montant_total) || Double.isInfinite(montant_total)) {
            throw new IllegalArgumentException("Le montant total de la vente est invalide.");
        }
        if (idfactureclient != null && !idfactureclient.trim().isEmpty()) {
            throw new IllegalArgumentException("Une vente brouillon ne peut pas encore être liée à une facture.");
        }

        if (!caisseAbsente) verifierCaisseActive(connexion);
        if ("RESERVATION".equals(type_vente)) {
            verifierReservationSource(connexion);
        } else if (idreservation != null && !idreservation.trim().isEmpty()) {
            throw new IllegalArgumentException("Une vente directe ne doit pas référencer une réservation.");
        }
    }

    private void verifierCaisseActive(Connection connexion) throws Exception {
        String sql = "SELECT ACTIF FROM CAISSE WHERE IDCAISSE = ?";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idcaisse);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("La caisse sélectionnée est introuvable.");
                if (rs.getInt(1) != 1) throw new Exception("La caisse sélectionnée est inactive.");
            }
        }
    }

    private void verifierReservationSource(Connection connexion) throws Exception {
        if (idreservation == null || idreservation.trim().isEmpty()) {
            throw new IllegalArgumentException("Une vente de type RESERVATION doit référencer sa réservation.");
        }
        String sql = "SELECT IDCLIENT, MONTANT_TOTAL FROM CINE_RESERVATION "
                + "WHERE IDRESERVATION = ? AND STATUT = 'EN_ATTENTE'";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idreservation);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("La réservation source est introuvable ou n'est plus en attente.");
                String clientReservation = rs.getString("IDCLIENT");
                if (clientReservation == null || !clientReservation.equals(idclient)) {
                    throw new Exception("Le client de la vente ne correspond pas à celui de la réservation.");
                }
                if (Double.compare(rs.getDouble("MONTANT_TOTAL"), montant_total) != 0) {
                    throw new Exception("Le montant de la vente ne correspond pas à celui de la réservation.");
                }
            }
        }
    }

    public String getIdvente() { return idvente; }
    public void setIdvente(String v) { idvente = v; }
    public Date getDaty() { return daty; }
    public void setDaty(Date v) { daty = v; }
    public String getType_vente() { return type_vente; }
    public void setType_vente(String v) { type_vente = v; }
    public String getStatut() { return statut; }
    public void setStatut(String v) { statut = v; }
    public String getIdclient() { return idclient; }
    public void setIdclient(String v) { idclient = v; }
    public String getIdcaisse() { return idcaisse; }
    public void setIdcaisse(String v) { idcaisse = v; }
    public String getIdreservation() { return idreservation; }
    public void setIdreservation(String v) { idreservation = v; }
    public String getIdfactureclient() { return idfactureclient; }
    public void setIdfactureclient(String v) { idfactureclient = v; }
    public double getMontant_total() { return montant_total; }
    public void setMontant_total(double v) { montant_total = v; }
}
