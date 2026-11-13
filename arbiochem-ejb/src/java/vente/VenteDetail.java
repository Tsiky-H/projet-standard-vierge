package vente;

import bean.ClassMAPTable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VenteDetail extends ClassMAPTable {
    private String idventedetails;
    private String idvente;
    private String idseance;
    private String idplace;
    private String idcategorieplace;
    private String designation;
    private double quantite = 1;
    private double prix_unitaire;
    private double montant_ligne;
    private String statut = "BLOQUEE";
    public VenteDetail() { setNomTable("VENTEDETAILS"); }
    @Override public String getTuppleID() { return idventedetails; }
    @Override public String getAttributIDName() { return "idventedetails"; }
    @Override public void construirePK(Connection c) throws Exception { preparePk("VTD", "getSeqVenteDetails"); setIdventedetails(makePK(c)); }

    @Override
    public void controler(Connection connexion) throws Exception {
        if (connexion == null) {
            throw new IllegalArgumentException("La connexion est obligatoire pour contrôler la ligne de vente.");
        }
        if (idvente == null || idvente.trim().isEmpty()
                || idseance == null || idseance.trim().isEmpty()
                || idplace == null || idplace.trim().isEmpty()
                || idcategorieplace == null || idcategorieplace.trim().isEmpty()) {
            throw new IllegalArgumentException("La vente, la séance, la place et sa catégorie sont obligatoires.");
        }
        if (designation == null || designation.trim().isEmpty()) {
            throw new IllegalArgumentException("La désignation de la ligne est obligatoire.");
        }
        if (quantite <= 0 || Double.isNaN(quantite) || Double.isInfinite(quantite)) {
            throw new IllegalArgumentException("La quantité de la ligne est invalide.");
        }
        if (prix_unitaire < 0 || Double.isNaN(prix_unitaire) || Double.isInfinite(prix_unitaire)
                || montant_ligne < 0 || Double.isNaN(montant_ligne) || Double.isInfinite(montant_ligne)) {
            throw new IllegalArgumentException("Le prix ou le montant de la ligne est invalide.");
        }
        if (!"BLOQUEE".equals(statut)) {
            throw new IllegalArgumentException("Une ligne de vente créée doit avoir le statut BLOQUEE.");
        }

        String sql = "SELECT v.STATUT AS STATUT_VENTE, s.STATUT AS STATUT_SEANCE, "
                + "p.IDSALLE AS SALLE_PLACE, s.IDSALLE AS SALLE_SEANCE, "
                + "p.ACTIF AS PLACE_ACTIVE, p.IDCATEGORIEPLACE AS CATEGORIE_PLACE, "
                + "cp.ACTIF AS CATEGORIE_ACTIVE "
                + "FROM VENTE v CROSS JOIN CINE_SEANCE s "
                + "CROSS JOIN CINE_PLACE p CROSS JOIN CINE_CATEGORIE_PLACE cp "
                + "WHERE v.IDVENTE = ? AND s.IDSEANCE = ? AND p.IDPLACE = ? "
                + "AND cp.IDCATEGORIEPLACE = ?";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idvente);
            ps.setString(2, idseance);
            ps.setString(3, idplace);
            ps.setString(4, idcategorieplace);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("La vente, la séance, la place ou la catégorie est introuvable.");
                if (!"BROUILLON".equals(rs.getString("STATUT_VENTE"))) {
                    throw new Exception("Une ligne ne peut être ajoutée qu'à une vente brouillon.");
                }
                String statutSeance = rs.getString("STATUT_SEANCE");
                if (!"PLANIFIEE".equals(statutSeance) && !"OUVERTE".equals(statutSeance)) {
                    throw new Exception("La séance n'est pas ouverte à la vente.");
                }
                if (!rs.getString("SALLE_PLACE").equals(rs.getString("SALLE_SEANCE"))) {
                    throw new Exception("La place n'appartient pas à la salle de cette séance.");
                }
                if (rs.getInt("PLACE_ACTIVE") != 1) throw new Exception("La place sélectionnée est inactive.");
                if (!idcategorieplace.equals(rs.getString("CATEGORIE_PLACE"))) {
                    throw new Exception("La catégorie ne correspond pas à celle de la place.");
                }
                if (rs.getInt("CATEGORIE_ACTIVE") != 1) {
                    throw new Exception("La catégorie de la place est inactive.");
                }
            }
        }
    }

    public String getIdventedetails() { return idventedetails; }
    public void setIdventedetails(String v) { idventedetails = v; }
    public String getIdvente() { return idvente; }
    public void setIdvente(String v) { idvente = v; }
    public String getIdseance() { return idseance; }
    public void setIdseance(String v) { idseance = v; }
    public String getIdplace() { return idplace; }
    public void setIdplace(String v) { idplace = v; }
    public String getIdcategorieplace() { return idcategorieplace; }
    public void setIdcategorieplace(String v) { idcategorieplace = v; }
    public String getDesignation() { return designation; }
    public void setDesignation(String v) { designation = v; }
    public double getQuantite() { return quantite; }
    public void setQuantite(double v) { quantite = v; }
    public double getPrix_unitaire() { return prix_unitaire; }
    public void setPrix_unitaire(double v) { prix_unitaire = v; }
    public double getMontant_ligne() { return montant_ligne; }
    public void setMontant_ligne(double v) { montant_ligne = v; }
    public String getStatut() { return statut; }
    public void setStatut(String v) { statut = v; }
}
