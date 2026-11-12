package cinema;

public class PlaceLib extends Place {

    private String idsallelib;
    private String idcategorieplacelib;
    private double prix_base;

    public PlaceLib() {
        setNomTable("CINE_PLACE_LIB");
    }

    public String getIdsallelib() { return idsallelib; }
    public void setIdsallelib(String idsallelib) { this.idsallelib = idsallelib; }
    public String getIdcategorieplacelib() { return idcategorieplacelib; }
    public void setIdcategorieplacelib(String idcategorieplacelib) { this.idcategorieplacelib = idcategorieplacelib; }
    public double getPrix_base() { return prix_base; }
    public void setPrix_base(double prix_base) { this.prix_base = prix_base; }
}
