package vente;

import bean.ClassMAPTable;
import java.sql.Connection;

public class VenteDetail extends ClassMAPTable {
    private String idventedetails;
    private String idvente;
    private String idseance;
    private String idplace;
    private String idcategorieplace;
    private String designation;
    private double quantite = 1;
    private double prix_unitaire;
    private double montant_ligne;
    private String statut = "BLOQUEE";
    public VenteDetail() { setNomTable("VENTEDETAILS"); }
    @Override public String getTuppleID() { return idventedetails; }
    @Override public String getAttributIDName() { return "idventedetails"; }
    @Override public void construirePK(Connection c) throws Exception { preparePk("VTD", "getSeqVenteDetails"); setIdventedetails(makePK(c)); }
    public String getIdventedetails() { return idventedetails; }
    public void setIdventedetails(String v) { idventedetails = v; }
    public String getIdvente() { return idvente; }
    public void setIdvente(String v) { idvente = v; }
    public String getIdseance() { return idseance; }
    public void setIdseance(String v) { idseance = v; }
    public String getIdplace() { return idplace; }
    public void setIdplace(String v) { idplace = v; }
    public String getIdcategorieplace() { return idcategorieplace; }
    public void setIdcategorieplace(String v) { idcategorieplace = v; }
    public String getDesignation() { return designation; }
    public void setDesignation(String v) { designation = v; }
    public double getQuantite() { return quantite; }
    public void setQuantite(double v) { quantite = v; }
    public double getPrix_unitaire() { return prix_unitaire; }
    public void setPrix_unitaire(double v) { prix_unitaire = v; }
    public double getMontant_ligne() { return montant_ligne; }
    public void setMontant_ligne(double v) { montant_ligne = v; }
    public String getStatut() { return statut; }
    public void setStatut(String v) { statut = v; }
}
