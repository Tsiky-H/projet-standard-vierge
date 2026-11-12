package paiement;

import bean.ClassMAPTable;
import java.sql.Connection;

/** Modèle cinéma de la table MODEPAIEMENT, adapté du modèle standard. */
public class ModePaiement extends ClassMAPTable {

    private String idmodepaiement;
    private String code;
    private String libelle;
    private int actif = 1;

    public ModePaiement() {
        setNomTable("MODEPAIEMENT");
    }

    @Override
    public String getTuppleID() {
        return idmodepaiement;
    }

    @Override
    public String getAttributIDName() {
        return "idmodepaiement";
    }

    @Override
    public void construirePK(Connection connexion) throws Exception {
        preparePk("MDP", "getSeqModePaiement");
        setIdmodepaiement(makePK(connexion));
    }

    @Override
    public String getValColLibelle() {
        return libelle;
    }

    public String getIdmodepaiement() { return idmodepaiement; }
    public void setIdmodepaiement(String idmodepaiement) { this.idmodepaiement = idmodepaiement; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public int getActif() { return actif; }
    public void setActif(int actif) { this.actif = actif; }
}
