package cinema;

import bean.ClassMAPTable;
import java.sql.Connection;

public class Place extends ClassMAPTable {

    private String idplace;
    private String idsalle;
    private String numero;
    private String rangee;
    private String idcategorieplace;
    private int actif = 1;

    public Place() {
        setNomTable("CINE_PLACE");
    }

    @Override
    public String getTuppleID() { return idplace; }

    @Override
    public String getAttributIDName() { return "idplace"; }

    @Override
    public void construirePK(Connection connexion) throws Exception {
        preparePk("PLC", "getSeqCinePlace");
        setIdplace(makePK(connexion));
    }

    public String getIdplace() { return idplace; }
    public void setIdplace(String idplace) { this.idplace = idplace; }
    public String getIdsalle() { return idsalle; }
    public void setIdsalle(String idsalle) { this.idsalle = idsalle; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getRangee() { return rangee; }
    public void setRangee(String rangee) { this.rangee = rangee; }
    public String getIdcategorieplace() { return idcategorieplace; }
    public void setIdcategorieplace(String idcategorieplace) { this.idcategorieplace = idcategorieplace; }
    public int getActif() { return actif; }
    public void setActif(int actif) { this.actif = actif; }
}
