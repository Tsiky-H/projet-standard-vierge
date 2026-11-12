package paiement;

import bean.ClassMAPTable;
import java.sql.Connection;
import java.sql.Date;

public class Paiement extends ClassMAPTable {
    private String idpaiement;
    private String numero_paiement;
    private Date daty;
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
    public String getIdpaiement() { return idpaiement; }
    public void setIdpaiement(String v) { idpaiement = v; }
    public String getNumero_paiement() { return numero_paiement; }
    public void setNumero_paiement(String v) { numero_paiement = v; }
    public Date getDaty() { return daty; }
    public void setDaty(Date v) { daty = v; }
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
