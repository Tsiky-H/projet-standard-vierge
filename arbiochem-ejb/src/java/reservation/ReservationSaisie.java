
package reservation;

import bean.ClassMAPTable;

public class ReservationSaisie extends ClassMAPTable {

    private String idsaisie;
    private String idclient;
    private String idseance;
    private String idmodepaiement_prevu;
    private String delai_expiration_minutes;

    public ReservationSaisie() {
        this.setNomTable("CINE_RESERVATION_SAISIE");
    }

    @Override
    public String getTuppleID() {
        return idsaisie;
    }

    @Override
    public String getAttributIDName() {
        return "idsaisie";
    }

    public String getIdsaisie() {
        return idsaisie;
    }

    public void setIdsaisie(String idsaisie) {
        this.idsaisie = idsaisie;
    }

    public String getIdclient() {
        return idclient;
    }

    public void setIdclient(String idclient) {
        this.idclient = idclient;
    }

    public String getIdseance() {
        return idseance;
    }

    public void setIdseance(String idseance) {
        this.idseance = idseance;
    }

    public String getIdmodepaiement_prevu() {
        return idmodepaiement_prevu;
    }

    public void setIdmodepaiement_prevu(String idmodepaiement_prevu) {
        this.idmodepaiement_prevu = idmodepaiement_prevu;
    }

    public String getDelai_expiration_minutes() { return delai_expiration_minutes; }
    public void setDelai_expiration_minutes(String delai_expiration_minutes) {
        this.delai_expiration_minutes = delai_expiration_minutes;
    }
}
