package reservation;

import bean.ClassMAPTable;
import java.sql.Connection;

public class ReservationDetail extends ClassMAPTable {
    private String idreservation_detail;
    private String idreservation;
    private String idseance;
    private int nb_places;
    private double montant_ligne;
    private String statut = "EN_ATTENTE";
    public ReservationDetail() { setNomTable("CINE_RESERVATION_DETAIL"); }
    @Override public String getTuppleID() { return idreservation_detail; }
    @Override public String getAttributIDName() { return "idreservation_detail"; }
    @Override public void construirePK(Connection c) throws Exception { preparePk("RSD", "getSeqCineReservationDetail"); setIdreservation_detail(makePK(c)); }
    public String getIdreservation_detail() { return idreservation_detail; }
    public void setIdreservation_detail(String v) { idreservation_detail = v; }
    public String getIdreservation() { return idreservation; }
    public void setIdreservation(String v) { idreservation = v; }
    public String getIdseance() { return idseance; }
    public void setIdseance(String v) { idseance = v; }
    public int getNb_places() { return nb_places; }
    public void setNb_places(int v) { nb_places = v; }
    public double getMontant_ligne() { return montant_ligne; }
    public void setMontant_ligne(double v) { montant_ligne = v; }
    public String getStatut() { return statut; }
    public void setStatut(String v) { statut = v; }
}
