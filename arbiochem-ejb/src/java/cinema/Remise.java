package cinema;

import bean.ClassMAPTable;

import java.sql.Connection;

public class Remise extends ClassMAPTable {

    private String id;
    private String cat_reservee;
    private int nombre;
    private double val;
    private String cat_remise;

    public Remise() {
        setNomTable("REMISE");
    }

    @Override
    public String getTuppleID() {
        return id;
    }

    @Override
    public String getAttributIDName() {
        return "id";
    }

    @Override
    public void construirePK(Connection connexion) throws Exception {
        preparePk("REM", "getSeqRemise");
        setId(makePK(connexion));
    }

    @Override
    public void controler(Connection connexion) throws Exception {
        if (cat_reservee == null || cat_reservee.trim().isEmpty()) {
            throw new IllegalArgumentException("La catégorie réservée est obligatoire.");
        }
        if (cat_remise == null || cat_remise.trim().isEmpty()) {
            throw new IllegalArgumentException("La catégorie bénéficiant de la remise est obligatoire.");
        }
        if (cat_reservee.equals(cat_remise)) {
            throw new IllegalArgumentException("La catégorie réservée et la catégorie remisée doivent être différentes.");
        }
        if (nombre <= 0) {
            throw new IllegalArgumentException("Le nombre minimum de places doit être supérieur à zéro.");
        }
        if (Double.isNaN(val) || Double.isInfinite(val) || val <= 0 || val > 100) {
            throw new IllegalArgumentException("Le pourcentage de remise doit être compris entre 0 et 100.");
        }

        cat_reservee = cat_reservee.trim();
        cat_remise = cat_remise.trim();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCat_reservee() {
        return cat_reservee;
    }

    public void setCat_reservee(String cat_reservee) {
        this.cat_reservee = cat_reservee;
    }

    public int getNombre() {
        return nombre;
    }

    public void setNombre(int nombre) {
        this.nombre = nombre;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }

    public String getCat_remise() {
        return cat_remise;
    }

    public void setCat_remise(String cat_remise) {
        this.cat_remise = cat_remise;
    }
}
