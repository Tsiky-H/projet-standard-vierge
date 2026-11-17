package vente;

/** Modèle de lecture enrichi des libellés associés à une vente. */
public class VenteLib extends Vente {

    private String idclientlib;
    private String idcaisselib;
    private String idreservationlib;
    private String idfactureclientlib;

    public VenteLib() {
        setNomTable("CINE_VENTE_LIB");
    }

    public String getIdclientlib() { return idclientlib; }
    public void setIdclientlib(String v) { idclientlib = v; }
    public String getIdcaisselib() { return idcaisselib; }
    public void setIdcaisselib(String v) { idcaisselib = v; }
    public String getIdreservationlib() { return idreservationlib; }
    public void setIdreservationlib(String v) { idreservationlib = v; }
    public String getIdfactureclientlib() { return idfactureclientlib; }
    public void setIdfactureclientlib(String v) { idfactureclientlib = v; }
}
