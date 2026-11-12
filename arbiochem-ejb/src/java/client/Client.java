package client;

import bean.ClassMAPTable;
import java.sql.Connection;
import java.sql.Date;

/** Modèle cinéma de la table CLIENT (adapté du modèle du projet standard). */
public class Client extends ClassMAPTable {

    private String idclient;
    private String codeclient;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private int actif = 1;
    private Date datycreation = new Date(System.currentTimeMillis());

    public Client() {
        setNomTable("CLIENT");
    }

    @Override
    public String getTuppleID() {
        return idclient;
    }

    @Override
    public String getAttributIDName() {
        return "idclient";
    }

    @Override
    public void construirePK(Connection connexion) throws Exception {
        preparePk("CLI", "getSeqClient");
        setIdclient(makePK(connexion));
        if (codeclient == null || codeclient.trim().isEmpty()) {
            setCodeclient(idclient);
        }
    }

    @Override
    public String getValColLibelle() {
        return ((nom == null) ? "" : nom)
                + ((prenom == null || prenom.trim().isEmpty()) ? "" : " " + prenom);
    }

    public String getIdclient() { return idclient; }
    public void setIdclient(String idclient) { this.idclient = idclient; }
    public String getCodeclient() { return codeclient; }
    public void setCodeclient(String codeclient) { this.codeclient = codeclient; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public int getActif() { return actif; }
    public void setActif(int actif) { this.actif = actif; }
    public Date getDatycreation() { return datycreation; }
    public void setDatycreation(Date datycreation) { this.datycreation = datycreation; }
}
