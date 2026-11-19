package cinema;

/** Modèle de lecture du plan de places associé à une séance. */
public class PlaceSeanceDispo extends PlaceLib {

    private String idseance;
    private String idfilm;
    private String idfilmlib;
    private java.sql.Timestamp dateheure_debut;
    private java.sql.Timestamp dateheure_fin;
    private String source_blocage;
    private String statut_occupation;
    private int disponible;
    private double prix_initial;
    private double taux_remise;
    private String idremise_appliquee;
    private double montant_remise;
    private double prix_final;

    public PlaceSeanceDispo() {
        setNomTable("CINE_PLACE_SEANCE_DISPO");
    }

    public String getIdseance() { return idseance; }
    public void setIdseance(String idseance) { this.idseance = idseance; }
    public String getIdfilm() { return idfilm; }
    public void setIdfilm(String idfilm) { this.idfilm = idfilm; }
    public String getIdfilmlib() { return idfilmlib; }
    public void setIdfilmlib(String idfilmlib) { this.idfilmlib = idfilmlib; }
    public java.sql.Timestamp getDateheure_debut() { return dateheure_debut; }
    public void setDateheure_debut(java.sql.Timestamp dateheure_debut) { this.dateheure_debut = dateheure_debut; }
    public java.sql.Timestamp getDateheure_fin() { return dateheure_fin; }
    public void setDateheure_fin(java.sql.Timestamp dateheure_fin) { this.dateheure_fin = dateheure_fin; }
    public String getSource_blocage() { return source_blocage; }
    public void setSource_blocage(String source_blocage) { this.source_blocage = source_blocage; }
    public String getStatut_occupation() { return statut_occupation; }
    public void setStatut_occupation(String statut_occupation) { this.statut_occupation = statut_occupation; }
    public int getDisponible() { return disponible; }
    public void setDisponible(int disponible) { this.disponible = disponible; }
    public double getPrix_initial() { return prix_initial; }
    public void setPrix_initial(double prix_initial) { this.prix_initial = prix_initial; }
    public double getTaux_remise() { return taux_remise; }
    public void setTaux_remise(double taux_remise) { this.taux_remise = taux_remise; }
    public String getIdremise_appliquee() { return idremise_appliquee; }
    public void setIdremise_appliquee(String idremise_appliquee) { this.idremise_appliquee = idremise_appliquee; }
    public double getMontant_remise() { return montant_remise; }
    public void setMontant_remise(double montant_remise) { this.montant_remise = montant_remise; }
    public double getPrix_final() { return prix_final; }
    public void setPrix_final(double prix_final) { this.prix_final = prix_final; }
}
