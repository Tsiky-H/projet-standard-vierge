package facturation;

/** Modèle de lecture enrichi des libellés associés à une facture client. */
public class FactureClientLib extends FactureClient {

    private String idclientlib;
    private String idfacture_originelib;

    public FactureClientLib() throws Exception {
        super();
        setNomTable("CINE_FACTURECLIENT_LIB");
    }

    public String getIdclientlib() { return idclientlib; }
    public void setIdclientlib(String v) { idclientlib = v; }
    public String getIdfacture_originelib() { return idfacture_originelib; }
    public void setIdfacture_originelib(String v) { idfacture_originelib = v; }
}
