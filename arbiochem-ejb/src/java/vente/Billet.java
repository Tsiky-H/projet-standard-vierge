package vente;

import bean.ClassMAPTable;
import java.sql.Connection;
import java.sql.Date;

public class Billet extends ClassMAPTable {
    private String idbillet;
    private String numero_billet;
    private String idventedetails;
    private String idreservation_place;
    private Date daty_emission;
    private String statut = "EMIS";
    private String qr_payload;
    public Billet() { setNomTable("CINE_BILLET"); }
    @Override public String getTuppleID() { return idbillet; }
    @Override public String getAttributIDName() { return "idbillet"; }
    @Override public void construirePK(Connection c) throws Exception { preparePk("BIL", "getSeqCineBillet"); setIdbillet(makePK(c)); }
    public String getIdbillet() { return idbillet; }
    public void setIdbillet(String v) { idbillet = v; }
    public String getNumero_billet() { return numero_billet; }
    public void setNumero_billet(String v) { numero_billet = v; }
    public String getIdventedetails() { return idventedetails; }
    public void setIdventedetails(String v) { idventedetails = v; }
    public String getIdreservation_place() { return idreservation_place; }
    public void setIdreservation_place(String v) { idreservation_place = v; }
    public Date getDaty_emission() { return daty_emission; }
    public void setDaty_emission(Date v) { daty_emission = v; }
    public String getStatut() { return statut; }
    public void setStatut(String v) { statut = v; }
    public String getQr_payload() { return qr_payload; }
    public void setQr_payload(String v) { qr_payload = v; }
}
