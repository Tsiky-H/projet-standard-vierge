package vente;

import bean.ClassMAPTable;
import cinema.ReglesCinema;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class Vente extends ClassMAPTable {
    private String idvente;
    private Timestamp daty;
    private String type_vente;
    private String statut = ReglesCinema.STATUT_BROUILLON;
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
        controlerChamps();
        controlerRelations(connexion);
    }

    private void controlerChamps() {
        if (daty == null) {
            throw new IllegalArgumentException("La date de vente est obligatoire.");
        }
        if (!ReglesCinema.TYPE_VENTE_DIRECTE.equals(type_vente)
                && !ReglesCinema.TYPE_VENTE_RESERVATION.equals(type_vente)) {
            throw new IllegalArgumentException("Le type de vente doit être DIRECTE ou RESERVATION.");
        }
        if (!ReglesCinema.STATUT_BROUILLON.equals(statut)) {
            throw new IllegalArgumentException("Une nouvelle vente doit avoir le statut BROUILLON.");
        }
        if (ReglesCinema.estVide(idclient)) {
            throw new IllegalArgumentException("Le client est obligatoire pour une vente.");
        }
        boolean caisseAbsente = ReglesCinema.estVide(idcaisse);
        if (caisseAbsente && !ReglesCinema.TYPE_VENTE_RESERVATION.equals(type_vente)) {
            throw new IllegalArgumentException("Seule une vente brouillon de réservation peut ne pas encore avoir de caisse.");
        }
        BigDecimal montant = ReglesCinema.montantDecimal(
                montant_total, "Le montant total de la vente est invalide.");
        if (montant.signum() < 0) {
            throw new IllegalArgumentException("Le montant total de la vente est invalide.");
        }
        if (!ReglesCinema.estVide(idfactureclient)) {
            throw new IllegalArgumentException("Une vente brouillon ne peut pas encore être liée à une facture.");
        }
    }

    private void controlerRelations(Connection connexion) throws Exception {
        if (!ReglesCinema.estVide(idcaisse)) verifierCaisseActive(connexion);
        if (ReglesCinema.TYPE_VENTE_RESERVATION.equals(type_vente)) {
            verifierReservationSource(connexion);
        } else {
            verifierClientActif(connexion);
            if (!ReglesCinema.estVide(idreservation)) {
                throw new IllegalArgumentException("Une vente directe ne doit pas référencer une réservation.");
            }
        }
    }

    private void verifierClientActif(Connection connexion) throws Exception {
        String sql = "SELECT ACTIF FROM CLIENT WHERE IDCLIENT = ?";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idclient.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Le client sélectionné est introuvable.");
                if (rs.getInt(1) != 1) throw new Exception("Le client sélectionné est inactif.");
            }
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
        String sql = "SELECT IDCLIENT, MONTANT_TOTAL, STATUT, DATE_EXPIRATION "
                + "FROM CINE_RESERVATION WHERE IDRESERVATION = ? FOR UPDATE";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idreservation.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("La réservation source est introuvable.");
                if (!ReglesCinema.STATUT_EN_ATTENTE.equals(rs.getString("STATUT"))) {
                    throw new Exception("La réservation source n'est plus en attente.");
                }
                Timestamp expiration = rs.getTimestamp("DATE_EXPIRATION");
                if (expiration == null || !expiration.after(new Timestamp(System.currentTimeMillis()))) {
                    throw new Exception("La réservation source a expiré.");
                }
                String clientReservation = rs.getString("IDCLIENT");
                if (clientReservation == null || !clientReservation.equals(idclient.trim())) {
                    throw new Exception("Le client de la vente ne correspond pas à celui de la réservation.");
                }
                BigDecimal montantReservation = rs.getBigDecimal("MONTANT_TOTAL");
                BigDecimal montantVente = ReglesCinema.montantDecimal(
                        montant_total, "Le montant total de la vente est invalide.");
                if (montantReservation == null || montantVente.compareTo(
                        montantReservation.setScale(2, RoundingMode.HALF_UP)) != 0) {
                    throw new Exception("Le montant de la vente ne correspond pas à celui de la réservation.");
                }
            }
        }
    }

    public String getIdvente() { return idvente; }
    public void setIdvente(String v) { idvente = v; }
    public Timestamp getDaty() { return daty; }
    public void setDaty(Timestamp v) { daty = v; }
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
