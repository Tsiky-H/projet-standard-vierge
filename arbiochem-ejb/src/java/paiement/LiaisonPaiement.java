package paiement;

import bean.ClassMAPTable;
import java.sql.Connection;
import java.sql.Date;

public class LiaisonPaiement extends ClassMAPTable {
    private String idliaisonpaiement;
    private String idpaiement;
    private String idfactureclient;
    private double montant_affecte;
    private Date daty;
    public LiaisonPaiement() { setNomTable("LIAISONPAIEMENT"); }
    @Override public String getTuppleID() { return idliaisonpaiement; }
    @Override public String getAttributIDName() { return "idliaisonpaiement"; }
    @Override public void construirePK(Connection c) throws Exception { preparePk("LPA", "getSeqLiaisonPaiement"); setIdliaisonpaiement(makePK(c)); }
    public String getIdliaisonpaiement() { return idliaisonpaiement; }
    public void setIdliaisonpaiement(String v) { idliaisonpaiement = v; }
    public String getIdpaiement() { return idpaiement; }
    public void setIdpaiement(String v) { idpaiement = v; }
    public String getIdfactureclient() { return idfactureclient; }
    public void setIdfactureclient(String v) { idfactureclient = v; }
    public double getMontant_affecte() { return montant_affecte; }
    public void setMontant_affecte(double v) { montant_affecte = v; }
    public Date getDaty() { return daty; }
    public void setDaty(Date v) { daty = v; }
}
