package reservation;

/** Modèle de lecture enrichi des libellés associés à une réservation. */
public class ReservationLib extends Reservation {

    private String idclientlib;
    private String idmodepaiement_prevulib;
    private String seanceslib;

    public ReservationLib() {
        setNomTable("CINE_RESERVATION_LIB");
    }

    public String getIdclientlib() { return idclientlib; }
    public void setIdclientlib(String v) { idclientlib = v; }
    public String getIdmodepaiement_prevulib() { return idmodepaiement_prevulib; }
    public void setIdmodepaiement_prevulib(String v) { idmodepaiement_prevulib = v; }
    public String getSeanceslib() { return seanceslib; }
    public void setSeanceslib(String v) { seanceslib = v; }
}
