package caisse;

import bean.ClassMAPTable;
import java.sql.Connection;

/** Modèle cinéma de la table CAISSE, adapté de la classe du projet standard. */
public class Caisse extends ClassMAPTable {

    private String idcaisse;
    private String code;
    private String libelle;
    private double solde_theorique;
    private int actif = 1;

    public Caisse() {
        setNomTable("CAISSE");
    }

    @Override
    public String getTuppleID() {
        return idcaisse;
    }

    @Override
    public String getAttributIDName() {
        return "idcaisse";
    }

    @Override
    public void construirePK(Connection connexion) throws Exception {
        preparePk("CAI", "getSeqCaisse");
        setIdcaisse(makePK(connexion));
    }

    @Override
    public String getValColLibelle() {
        return libelle;
    }

    @Override
    public String[] getMotCles() {
        return new String[] {"idcaisse", "code", "libelle"};
    }

    @Override
    public String[] getValMotCles() {
        return new String[] {"idcaisse", "code", "libelle"};
    }

    public String getIdcaisse() { return idcaisse; }
    public void setIdcaisse(String idcaisse) { this.idcaisse = idcaisse; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public double getSolde_theorique() { return solde_theorique; }
    public void setSolde_theorique(double solde_theorique) { this.solde_theorique = solde_theorique; }
    public int getActif() { return actif; }
    public void setActif(int actif) { this.actif = actif; }
}
