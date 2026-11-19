package cinema;

import bean.ClassMAPTable;
import java.sql.Connection;

public class Film extends ClassMAPTable {

    private String idfilm;
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
        preparePk("FIL", "getSeqCineFilm");
        setIdfilm(makePK(connexion));
    }

    @Override
    public void controler(Connection connexion) throws Exception {
        if (titre == null || titre.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre du film est obligatoire.");
        }
        if (duree_minutes <= 0) {
            throw new IllegalArgumentException("La durée du film doit être supérieure à zéro minute.");
        }
        if (classification_age < 0) {
            throw new IllegalArgumentException("La classification d'âge ne peut pas être négative.");
        }
        if (actif != 0 && actif != 1) {
            throw new IllegalArgumentException("L'état actif du film est invalide.");
        }
        titre = titre.trim();
    }

    public String getIdfilm() { return idfilm; }
    public void setIdfilm(String idfilm) { this.idfilm = idfilm; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public int getDuree_minutes() { return duree_minutes; }
    public void setDuree_minutes(int duree_minutes) { this.duree_minutes = duree_minutes; }
    public int getClassification_age() { return classification_age; }
    public void setClassification_age(int classification_age) { this.classification_age = classification_age; }
    public int getActif() { return actif; }
    public void setActif(int actif) { this.actif = actif; }
}
