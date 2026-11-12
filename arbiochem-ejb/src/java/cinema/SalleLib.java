package cinema;

public class SalleLib extends Salle {

    private int nombre_places;

    public SalleLib() {
        setNomTable("CINE_SALLE_LIB");
    }

    public int getNombre_places() { return nombre_places; }
    public void setNombre_places(int nombre_places) { this.nombre_places = nombre_places; }
}
