package cinema;

import bean.ClassMAPTable;
import java.sql.Connection;

public class CategoriePlace extends ClassMAPTable {

    private String idcategorieplace;
    private String code;
    private String libelle;
    private double prix_base;
    private int actif = 1;

    public CategoriePlace() {
        setNomTable("CINE_CATEGORIE_PLACE");
    }

    @Override
    public String getTuppleID() { return idcategorieplace; }

    @Override
    public String getAttributIDName() { return "idcategorieplace"; }

    @Override
    public void construirePK(Connection connexion) throws Exception {
        preparePk("CAT", "getSeqCineCategoriePlace");
        setIdcategorieplace(makePK(connexion));
    }

    public String getIdcategorieplace() { return idcategorieplace; }
    public void setIdcategorieplace(String idcategorieplace) { this.idcategorieplace = idcategorieplace; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public double getPrix_base() { return prix_base; }
    public void setPrix_base(double prix_base) { this.prix_base = prix_base; }
    public int getActif() { return actif; }
    public void setActif(int actif) { this.actif = actif; }
}
