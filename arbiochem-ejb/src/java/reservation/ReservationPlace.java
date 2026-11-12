package reservation;

import bean.ClassMAPTable;
import java.sql.Connection;
import java.sql.Timestamp;

public class ReservationPlace extends ClassMAPTable {
    private String idreservation_place;
    private String idreservation_detail;
    private String idseance;
    private String idplace;
    private String idcategorieplace;
    private double prix_unitaire;
    private String statut = "BLOQUEE";
    private Timestamp date_blocage;
    private Timestamp date_expiration;
    private Timestamp date_validation;
    public ReservationPlace() { setNomTable("CINE_RESERVATION_PLACE"); }
    @Override public String getTuppleID() { return idreservation_place; }
    @Override public String getAttributIDName() { return "idreservation_place"; }
    @Override public void construirePK(Connection c) throws Exception { preparePk("RSP", "getSeqCineReservationPlace"); setIdreservation_place(makePK(c)); }
    public String getIdreservation_place() { return idreservation_place; }
    public void setIdreservation_place(String v) { idreservation_place = v; }
    public String getIdreservation_detail() { return idreservation_detail; }
    public void setIdreservation_detail(String v) { idreservation_detail = v; }
    public String getIdseance() { return idseance; }
    public void setIdseance(String v) { idseance = v; }
    public String getIdplace() { return idplace; }
    public void setIdplace(String v) { idplace = v; }
    public String getIdcategorieplace() { return idcategorieplace; }
    public void setIdcategorieplace(String v) { idcategorieplace = v; }
    public double getPrix_unitaire() { return prix_unitaire; }
    public void setPrix_unitaire(double v) { prix_unitaire = v; }
    public String getStatut() { return statut; }
    public void setStatut(String v) { statut = v; }
    public Timestamp getDate_blocage() { return date_blocage; }
    public void setDate_blocage(Timestamp v) { date_blocage = v; }
    public Timestamp getDate_expiration() { return date_expiration; }
    public void setDate_expiration(Timestamp v) { date_expiration = v; }
    public Timestamp getDate_validation() { return date_validation; }
    public void setDate_validation(Timestamp v) { date_validation = v; }
}
