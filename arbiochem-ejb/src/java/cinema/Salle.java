package cinema;

import bean.ClassMAPTable;
import java.sql.Connection;

/** Modèle APJ de la table CINE_SALLE. */
public class Salle extends ClassMAPTable {

    private String idSalle;
    private String code;
    private String libelle;
    private int capacite;
    private int actif;

    public Salle() {
        setNomTable("CINE_SALLE");
    }

    @Override
    public String getTuppleID() {
        return idSalle;
    }

    @Override
    public String getAttributIDName() {
        return "idSalle";
    }

    @Override
    public void construirePK(Connection connexion) throws Exception {
        preparePk("SAL", "getSeqCineSalle");
        setIdSalle(makePK(connexion));
    }

    public String getIdSalle() {
        return idSalle;
    }

    public void setIdSalle(String idSalle) {
        this.idSalle = idSalle;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public int getActif() {
        return actif;
    }

    public void setActif(int actif) {
        this.actif = actif;
    }
}
