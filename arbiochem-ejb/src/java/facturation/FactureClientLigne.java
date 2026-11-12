package facturation;

import bean.ClassFille;
import java.sql.Connection;

public class FactureClientLigne extends ClassFille {

    private String idfactureclient_ligne;
    private String idfactureclient;
    private String designation;
    private double quantite;
    private double prix_unitaire;
    private double taux_tva;
    private double montant_ht;
    private double montant_tva;
    private double montant_ttc;
    private String idreservation_place;
    private String idventedetails;
    private String idfactureclient_ligne_origine;
    private int etat = 1;

    public FactureClientLigne() throws Exception {
        setNomTable("FACTURECLIENT_LIGNE");
        setLiaisonMere("idfactureclient");
        setNomClasseMere("facturation.FactureClient");
    }

    @Override
    public String getTuppleID() { return idfactureclient_ligne; }

    @Override
    public String getAttributIDName() { return "idfactureclient_ligne"; }

    @Override
    public void construirePK(Connection connexion) throws Exception {
        preparePk("FCL", "getSeqFactureClientLigne");
        setIdfactureclient_ligne(makePK(connexion));
    }

    @Override
    public String getNomClasseMere() { return "facturation.FactureClient"; }

    @Override
    public String getLiaisonMere() { return "idfactureclient"; }

    public String getIdfactureclient_ligne() { return idfactureclient_ligne; }
    public void setIdfactureclient_ligne(String v) { idfactureclient_ligne = v; }
    public String getIdfactureclient() { return idfactureclient; }
    public void setIdfactureclient(String v) { idfactureclient = v; }
    public String getDesignation() { return designation; }
    public void setDesignation(String v) { designation = v; }
    public double getQuantite() { return quantite; }
    public void setQuantite(double v) { quantite = v; }
    public double getPrix_unitaire() { return prix_unitaire; }
    public void setPrix_unitaire(double v) { prix_unitaire = v; }
    public double getTaux_tva() { return taux_tva; }
    public void setTaux_tva(double v) { taux_tva = v; }
    public double getMontant_ht() { return montant_ht; }
    public void setMontant_ht(double v) { montant_ht = v; }
    public double getMontant_tva() { return montant_tva; }
    public void setMontant_tva(double v) { montant_tva = v; }
    public double getMontant_ttc() { return montant_ttc; }
    public void setMontant_ttc(double v) { montant_ttc = v; }
    public String getIdreservation_place() { return idreservation_place; }
    public void setIdreservation_place(String v) { idreservation_place = v; }
    public String getIdventedetails() { return idventedetails; }
    public void setIdventedetails(String v) { idventedetails = v; }
    public String getIdfactureclient_ligne_origine() { return idfactureclient_ligne_origine; }
    public void setIdfactureclient_ligne_origine(String v) { idfactureclient_ligne_origine = v; }
    @Override public int getEtat() { return etat; }
    @Override public void setEtat(int v) { etat = v; }
}
