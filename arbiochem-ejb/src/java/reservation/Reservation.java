package reservation;

import bean.ClassMAPTable;
import java.sql.Connection;
import java.sql.Timestamp;

public class Reservation extends ClassMAPTable {

    private String idreservation;
    private String numero_reservation;
    private String idclient;
    private String idmodepaiement_prevu;
    private Timestamp date_reservation;
    private Timestamp date_expiration;
    private String statut = "EN_ATTENTE";
    private int nb_places_total;
    private double montant_total;
    private String remarque;

    public Reservation() { setNomTable("CINE_RESERVATION"); }
    @Override public String getTuppleID() { return idreservation; }
    @Override public String getAttributIDName() { return "idreservation"; }
    @Override public void construirePK(Connection c) throws Exception { preparePk("RES", "getSeqCineReservation"); setIdreservation(makePK(c)); }
    public String getIdreservation() { return idreservation; }
    public void setIdreservation(String v) { idreservation = v; }
    public String getNumero_reservation() { return numero_reservation; }
    public void setNumero_reservation(String v) { numero_reservation = v; }
    public String getIdclient() { return idclient; }
    public void setIdclient(String v) { idclient = v; }
    public String getIdmodepaiement_prevu() { return idmodepaiement_prevu; }
    public void setIdmodepaiement_prevu(String v) { idmodepaiement_prevu = v; }
    public Timestamp getDate_reservation() { return date_reservation; }
    public void setDate_reservation(Timestamp v) { date_reservation = v; }
    public Timestamp getDate_expiration() { return date_expiration; }
    public void setDate_expiration(Timestamp v) { date_expiration = v; }
    public String getStatut() { return statut; }
    public void setStatut(String v) { statut = v; }
    public int getNb_places_total() { return nb_places_total; }
    public void setNb_places_total(int v) { nb_places_total = v; }
    public double getMontant_total() { return montant_total; }
    public void setMontant_total(double v) { montant_total = v; }
    public String getRemarque() { return remarque; }
    public void setRemarque(String v) { remarque = v; }
}
