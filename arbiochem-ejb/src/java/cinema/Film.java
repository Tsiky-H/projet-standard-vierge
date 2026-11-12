package cinema;

import bean.ClassMAPTable;
import java.sql.Connection;

public class Film extends ClassMAPTable {

    private String idfilm;
    private String code;
    private String titre;
    private int duree_minutes;
    private int classification_age;
    private int actif = 1;

    public Film() {
        setNomTable("CINE_FILM");
    }

    @Override
    public String getTuppleID() { return idfilm; }

    @Override
    public String getAttributIDName() { return "idfilm"; }

    @Override
    public void construirePK(Connection connexion) throws Exception {
        preparePk("FLM", "getSeqCineFilm");
        setIdfilm(makePK(connexion));
    }

    public String getIdfilm() { return idfilm; }
    public void setIdfilm(String idfilm) { this.idfilm = idfilm; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public int getDuree_minutes() { return duree_minutes; }
    public void setDuree_minutes(int duree_minutes) { this.duree_minutes = duree_minutes; }
    public int getClassification_age() { return classification_age; }
    public void setClassification_age(int classification_age) { this.classification_age = classification_age; }
    public int getActif() { return actif; }
    public void setActif(int actif) { this.actif = actif; }
}
