package cinema;

public class SeanceLib extends Seance {

    private String idfilmlib;
    private String idsallelib;
    private String libelle_seance;

    public SeanceLib() {
        setNomTable("CINE_SEANCE_LIB");
    }

    public String getIdfilmlib() {
        return idfilmlib;
    }

    public void setIdfilmlib(String idfilmlib) {
        this.idfilmlib = idfilmlib;
    }

    public String getIdsallelib() {
        return idsallelib;
    }

    public void setIdsallelib(String idsallelib) {
        this.idsallelib = idsallelib;
    }

    public String getLibelle_seance() {
        return libelle_seance;
    }

    public void setLibelle_seance(String libelle_seance) {
        this.libelle_seance = libelle_seance;
    }
}
