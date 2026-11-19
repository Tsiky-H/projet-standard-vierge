package cinema;

import bean.ClassMAPTable;
import java.sql.Connection;

/** Modèle APJ de la table CINE_SALLE. */
public class Salle extends ClassMAPTable {

    private String idSalle;
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

    @Override
    public void controler(Connection connexion) throws Exception {
        if (libelle == null || libelle.trim().isEmpty()) {
            throw new IllegalArgumentException("Le libellé de la salle est obligatoire.");
        }
        if (capacite <= 0) {
            throw new IllegalArgumentException("La salle doit contenir au moins une place.");
        }
        if (capacite > 999999) {
            throw new IllegalArgumentException("La capacité maximale d'une salle est de 999999 places.");
        }
        if (actif != 0 && actif != 1) {
            throw new IllegalArgumentException("L'état actif de la salle est invalide.");
        }
        libelle = libelle.trim();
    }

    public String getIdSalle() {
        return idSalle;
    }

    public void setIdSalle(String idSalle) {
        this.idSalle = idSalle;
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
