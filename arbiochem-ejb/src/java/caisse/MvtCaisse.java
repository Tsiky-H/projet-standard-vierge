package caisse;

import bean.ClassEtat;
import cinema.ReglesCinema;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.math.BigDecimal;

/** Mouvement de caisse cinéma, sans dépendances comptables Arbiochem. */
public class MvtCaisse extends ClassEtat {

    private String idmvtcaisse;
    private String idcaisse;
    private Date daty = new Date(System.currentTimeMillis());
    private String type_mouvement;
    private double montant;
    private String statut = ReglesCinema.STATUT_MOUVEMENT_ACTIF;
    private String idpaiement;
    private String idfactureclient;
    private String idmvtcaisse_origine;
    private String observation;

    public MvtCaisse() {
        setNomTable("MVTCAISSE");
    }

    @Override
    public String getTuppleID() {
        return idmvtcaisse;
    }

    @Override
    public String getAttributIDName() {
        return "idmvtcaisse";
    }

    @Override
    public void construirePK(Connection connexion) throws Exception {
        preparePk("MTC", "getSeqMvtCaisse");
        setIdmvtcaisse(makePK(connexion));
    }

    @Override
    public void controler(Connection connexion) throws Exception {
        super.controler(connexion);
        if (daty == null || ReglesCinema.estVide(idcaisse)
                || ReglesCinema.estVide(idpaiement) || ReglesCinema.estVide(idfactureclient)) {
            throw new IllegalArgumentException("La date, la caisse, le paiement et la facture sont obligatoires.");
        }
        BigDecimal montantValide = ReglesCinema.montantDecimal(
                montant, "Le montant du mouvement est invalide.");
        if (montantValide.signum() <= 0) {
            throw new Exception("Le montant du mouvement doit être positif.");
        }
        if (!isEncaissement() && !isDecaissement()) {
            throw new Exception("Le type doit être ENCAISSEMENT ou DECAISSEMENT.");
        }
        if (!ReglesCinema.STATUT_MOUVEMENT_ACTIF.equals(statut)
                && !ReglesCinema.STATUT_MOUVEMENT_ANNULE.equals(statut)
                && !ReglesCinema.STATUT_MOUVEMENT_REMPLACE.equals(statut)) {
            throw new IllegalArgumentException("Le statut du mouvement de caisse est invalide.");
        }
        verifierCaisseActive(connexion);
    }

    private void verifierCaisseActive(Connection connexion) throws Exception {
        String sql = "SELECT ACTIF FROM CAISSE WHERE IDCAISSE = ?";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idcaisse);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("La caisse du mouvement est introuvable.");
                if (rs.getInt(1) != 1) throw new Exception("La caisse du mouvement est inactive.");
            }
        }
    }

    public boolean isEncaissement() {
        return ReglesCinema.TYPE_MOUVEMENT_ENCAISSEMENT.equalsIgnoreCase(type_mouvement);
    }

    public boolean isDecaissement() {
        return ReglesCinema.TYPE_MOUVEMENT_DECAISSEMENT.equalsIgnoreCase(type_mouvement);
    }

    public String getIdmvtcaisse() { return idmvtcaisse; }
    public void setIdmvtcaisse(String idmvtcaisse) { this.idmvtcaisse = idmvtcaisse; }
    public String getIdcaisse() { return idcaisse; }
    public void setIdcaisse(String idcaisse) { this.idcaisse = idcaisse; }
    public Date getDaty() { return daty; }
    public void setDaty(Date daty) { this.daty = daty; }
    public String getType_mouvement() { return type_mouvement; }
    public void setType_mouvement(String type_mouvement) { this.type_mouvement = type_mouvement; }
    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getIdpaiement() { return idpaiement; }
    public void setIdpaiement(String idpaiement) { this.idpaiement = idpaiement; }
    public String getIdfactureclient() { return idfactureclient; }
    public void setIdfactureclient(String idfactureclient) { this.idfactureclient = idfactureclient; }
    public String getIdmvtcaisse_origine() { return idmvtcaisse_origine; }
    public void setIdmvtcaisse_origine(String idmvtcaisse_origine) { this.idmvtcaisse_origine = idmvtcaisse_origine; }
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
}
