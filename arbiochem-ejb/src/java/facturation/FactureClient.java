package facturation;

import bean.ClassMere;
import java.sql.Connection;
import java.sql.Date;

public class FactureClient extends ClassMere {

    private String idfactureclient;
    private String numero_facture;
    private String type_document = "FACTURE";
    private String idfacture_origine;
    private String idclient;
    private Date daty;
    private String statut = "BROUILLON";
    private int etat = 1;
    private double montant_ht;
    private double montant_tva;
    private double montant_ttc;

    public FactureClient() throws Exception {
        setNomTable("FACTURECLIENT");
        setLiaisonFille("idfactureclient");
        setNomClasseFille("facturation.FactureClientLigne");
    }

    @Override
    public String getTuppleID() { return idfactureclient; }

    @Override
    public String getAttributIDName() { return "idfactureclient"; }

    @Override
    public void construirePK(Connection connexion) throws Exception {
        preparePk("FAC", "getSeqFactureClient");
        setIdfactureclient(makePK(connexion));
    }

    public String getIdfactureclient() { return idfactureclient; }
    public void setIdfactureclient(String v) { idfactureclient = v; }
    public String getNumero_facture() { return numero_facture; }
    public void setNumero_facture(String v) { numero_facture = v; }
    public String getType_document() { return type_document; }
    public void setType_document(String v) { type_document = v; }
    public String getIdfacture_origine() { return idfacture_origine; }
    public void setIdfacture_origine(String v) { idfacture_origine = v; }
    public String getIdclient() { return idclient; }
    public void setIdclient(String v) { idclient = v; }
    public Date getDaty() { return daty; }
    public void setDaty(Date v) { daty = v; }
    public String getStatut() { return statut; }
    public void setStatut(String v) { statut = v; }
    @Override public int getEtat() { return etat; }
    @Override public void setEtat(int v) { etat = v; }
    public double getMontant_ht() { return montant_ht; }
    public void setMontant_ht(double v) { montant_ht = v; }
    public double getMontant_tva() { return montant_tva; }
    public void setMontant_tva(double v) { montant_tva = v; }
    public double getMontant_ttc() { return montant_ttc; }
    public void setMontant_ttc(double v) { montant_ttc = v; }
}
