package vente;

import bean.ClassMAPTable;
import cinema.ReglesCinema;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    // private String statut = ReglesCinema.STATUT_BLOQUEE;
    private String statut;

    public VenteDetail() {
        setNomTable("VENTEDETAILS");
    }

    @Override
    public String getTuppleID() {
        return idventedetails;
    }

    @Override
    public String getAttributIDName() {
        return "idventedetails";
    }

    @Override
    public void construirePK(Connection c) throws Exception {
        preparePk("VTD", "getSeqVenteDetails");
        setIdventedetails(makePK(c));
    }

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
        if (Double.isNaN(quantite) || Double.isInfinite(quantite)
                || BigDecimal.valueOf(quantite).setScale(2, RoundingMode.HALF_UP).signum() <= 0) {
            throw new IllegalArgumentException("La quantité de la ligne est invalide.");
        }
        BigDecimal prix = ReglesCinema.montantDecimal(
                prix_unitaire, "Le prix unitaire de la ligne est invalide.");
        BigDecimal montant = ReglesCinema.montantDecimal(
                montant_ligne, "Le montant de la ligne est invalide.");
        BigDecimal quantiteDecimal = BigDecimal.valueOf(quantite).setScale(2, RoundingMode.HALF_UP);
        if (prix.signum() < 0 || montant.signum() < 0
                || quantiteDecimal.multiply(prix).setScale(2, RoundingMode.HALF_UP).compareTo(montant) != 0) {
            throw new IllegalArgumentException("Le prix ou le montant de la ligne est invalide.");
        }
        if (!ReglesCinema.STATUT_BLOQUEE.equals(statut)) {
            throw new IllegalArgumentException("Une ligne de vente créée doit avoir le statut BLOQUEE.");
        }

        String sql = "SELECT v.STATUT AS STATUT_VENTE, s.STATUT AS STATUT_SEANCE, s.DATEHEURE_DEBUT, "
                + "p.IDSALLE AS SALLE_PLACE, s.IDSALLE AS SALLE_SEANCE, "
                + "p.ACTIF AS PLACE_ACTIVE, p.IDCATEGORIEPLACE AS CATEGORIE_PLACE, "
                + "cp.ACTIF AS CATEGORIE_ACTIVE "
                + "FROM VENTE v CROSS JOIN CINE_SEANCE s "
                + "CROSS JOIN CINE_PLACE p CROSS JOIN CINE_CATEGORIE_PLACE cp "
                + "WHERE v.IDVENTE = ? AND s.IDSEANCE = ? AND p.IDPLACE = ? "
                + "AND cp.IDCATEGORIEPLACE = ? FOR UPDATE OF p.IDPLACE";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idvente);
            ps.setString(2, idseance);
            ps.setString(3, idplace);
            ps.setString(4, idcategorieplace);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    throw new Exception("La vente, la séance, la place ou la catégorie est introuvable.");
                if (!ReglesCinema.STATUT_BROUILLON.equals(rs.getString("STATUT_VENTE"))) {
                    throw new Exception("Une ligne ne peut être ajoutée qu'à une vente brouillon.");
                }
                String statutSeance = rs.getString("STATUT_SEANCE");
                if (!ReglesCinema.STATUT_PLANIFIEE.equals(statutSeance)
                        && !ReglesCinema.STATUT_OUVERTE.equals(statutSeance)) {
                    throw new Exception("La séance n'est pas ouverte à la vente.");
                }
                if (rs.getTimestamp("DATEHEURE_DEBUT") == null
                        || !rs.getTimestamp("DATEHEURE_DEBUT")
                                .after(new java.sql.Timestamp(System.currentTimeMillis()))) {
                    throw new Exception("La séance a déjà commencé.");
                }
                if (!rs.getString("SALLE_PLACE").equals(rs.getString("SALLE_SEANCE"))) {
                    throw new Exception("La place n'appartient pas à la salle de cette séance.");
                }
                if (rs.getInt("PLACE_ACTIVE") != 1)
                    throw new Exception("La place sélectionnée est inactive.");
                if (!idcategorieplace.equals(rs.getString("CATEGORIE_PLACE"))) {
                    throw new Exception("La catégorie ne correspond pas à celle de la place.");
                }
                if (rs.getInt("CATEGORIE_ACTIVE") != 1) {
                    throw new Exception("La catégorie de la place est inactive.");
                }
            }
        }
        verifierPlaceDisponible(connexion);
    }

    private void verifierPlaceDisponible(Connection connexion) throws Exception {
        // Une place bloquée par la réservation liée à CETTE vente n'est pas un conflit
        // :
        // la ligne de vente brouillon ne fait que reprendre cette place.
        String sqlReservation = "SELECT 1 FROM CINE_RESERVATION_PLACE rp WHERE rp.IDSEANCE = ? AND rp.IDPLACE = ? "
                + "AND rp.STATUT IN ('" + ReglesCinema.STATUT_BLOQUEE + "', '" + ReglesCinema.STATUT_ACHETEE + "') "
                + "AND NOT EXISTS (SELECT 1 FROM CINE_RESERVATION_DETAIL rd "
                + "JOIN VENTE v ON v.IDRESERVATION = rd.IDRESERVATION "
                + "WHERE rd.IDRESERVATION_DETAIL = rp.IDRESERVATION_DETAIL AND v.IDVENTE = ?) "
                + "AND ROWNUM = 1";
        try (PreparedStatement ps = connexion.prepareStatement(sqlReservation)) {
            ps.setString(1, idseance);
            ps.setString(2, idplace);
            ps.setString(3, idvente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new Exception("La place est déjà bloquée ou vendue pour cette séance.");
                }
            }
        }

        // Inchangé : une autre ligne de vente active sur la même place reste un
        // conflit.
        String sqlVente = "SELECT 1 FROM VENTEDETAILS WHERE IDSEANCE = ? AND IDPLACE = ? "
                + "AND STATUT IN ('" + ReglesCinema.STATUT_BLOQUEE + "', '" + ReglesCinema.STATUT_ACHETEE
                + "') AND ROWNUM = 1";
        try (PreparedStatement ps = connexion.prepareStatement(sqlVente)) {
            ps.setString(1, idseance);
            ps.setString(2, idplace);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new Exception("La place est déjà bloquée ou vendue pour cette séance.");
                }
            }
        }
    }

    public String getIdventedetails() {
        return idventedetails;
    }

    public void setIdventedetails(String v) {
        idventedetails = v;
    }

    public String getIdvente() {
        return idvente;
    }

    public void setIdvente(String v) {
        idvente = v;
    }

    public String getIdseance() {
        return idseance;
    }

    public void setIdseance(String v) {
        idseance = v;
    }

    public String getIdplace() {
        return idplace;
    }

    public void setIdplace(String v) {
        idplace = v;
    }

    public String getIdcategorieplace() {
        return idcategorieplace;
    }

    public void setIdcategorieplace(String v) {
        idcategorieplace = v;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String v) {
        designation = v;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double v) {
        quantite = v;
    }

    public double getPrix_unitaire() {
        return prix_unitaire;
    }

    public void setPrix_unitaire(double v) {
        prix_unitaire = v;
    }

    public double getMontant_ligne() {
        return montant_ligne;
    }

    public void setMontant_ligne(double v) {
        montant_ligne = v;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String v) {
        statut = v;
    }
}
