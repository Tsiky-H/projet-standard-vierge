package cinema;

public class SeanceLib extends Seance {

    private String idfilmlib;
    private String idsallelib;

    public SeanceLib() {
        setNomTable("CINE_SEANCE_LIB");
    }

    public String getIdfilmlib() { return idfilmlib; }
    public void setIdfilmlib(String idfilmlib) { this.idfilmlib = idfilmlib; }
    public String getIdsallelib() { return idsallelib; }
    public void setIdsallelib(String idsallelib) { this.idsallelib = idsallelib; }
}
