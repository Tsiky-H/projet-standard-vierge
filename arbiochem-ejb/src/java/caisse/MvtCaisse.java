package caisse;

import bean.ClassEtat;
import java.sql.Connection;
import java.sql.Date;

/** Mouvement de caisse cinéma, sans dépendances comptables Arbiochem. */
public class MvtCaisse extends ClassEtat {

    private String idmvtcaisse;
    private String idcaisse;
    private Date daty = new Date(System.currentTimeMillis());
    private String type_mouvement;
    private double montant;
    private String statut = "ACTIF";
    private String idpaiement;
    private String idfactureclient;
    private String idmvtcaisse_origine;
    private String observation;

    public MvtCaisse() {
        setNomTable("MVTCAISSE");
    }

    @Override
    public String getTuppleID() {
        return idmvtcaisse;
    }

    @Override
    public String getAttributIDName() {
        return "idmvtcaisse";
    }

    @Override
    public void construirePK(Connection connexion) throws Exception {
        preparePk("MTC", "getSeqMvtCaisse");
        setIdmvtcaisse(makePK(connexion));
    }

    @Override
    public void controler(Connection connexion) throws Exception {
        super.controler(connexion);
        if (montant <= 0) {
            throw new Exception("Le montant du mouvement doit être positif.");
        }
        if (!isEncaissement() && !isDecaissement()) {
            throw new Exception("Le type doit être ENCAISSEMENT ou DECAISSEMENT.");
        }
    }

    public boolean isEncaissement() {
        return "ENCAISSEMENT".equalsIgnoreCase(type_mouvement);
    }

    public boolean isDecaissement() {
        return "DECAISSEMENT".equalsIgnoreCase(type_mouvement);
    }

    public String getIdmvtcaisse() { return idmvtcaisse; }
    public void setIdmvtcaisse(String idmvtcaisse) { this.idmvtcaisse = idmvtcaisse; }
    public String getIdcaisse() { return idcaisse; }
    public void setIdcaisse(String idcaisse) { this.idcaisse = idcaisse; }
    public Date getDaty() { return daty; }
    public void setDaty(Date daty) { this.daty = daty; }
    public String getType_mouvement() { return type_mouvement; }
    public void setType_mouvement(String type_mouvement) { this.type_mouvement = type_mouvement; }
    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getIdpaiement() { return idpaiement; }
    public void setIdpaiement(String idpaiement) { this.idpaiement = idpaiement; }
    public String getIdfactureclient() { return idfactureclient; }
    public void setIdfactureclient(String idfactureclient) { this.idfactureclient = idfactureclient; }
    public String getIdmvtcaisse_origine() { return idmvtcaisse_origine; }
    public void setIdmvtcaisse_origine(String idmvtcaisse_origine) { this.idmvtcaisse_origine = idmvtcaisse_origine; }
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
}
