package vente;

import bean.ClassMAPTable;
import java.sql.Connection;
import java.sql.Date;

public class Vente extends ClassMAPTable {
    private String idvente;
    private String numero_vente;
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
    public String getIdvente() { return idvente; }
    public void setIdvente(String v) { idvente = v; }
    public String getNumero_vente() { return numero_vente; }
    public void setNumero_vente(String v) { numero_vente = v; }
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
