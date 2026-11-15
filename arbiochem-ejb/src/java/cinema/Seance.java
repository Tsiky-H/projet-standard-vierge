package cinema;

import bean.ClassMAPTable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class Seance extends ClassMAPTable {

    private String idseance;
    private String idfilm;
    private String idsalle;
    private Timestamp dateheure_debut;
    private Timestamp dateheure_fin;
    private String statut = ReglesCinema.STATUT_PLANIFIEE;

    public Seance() {
        setNomTable("CINE_SEANCE");
    }

    @Override
    public String getTuppleID() { return idseance; }

    @Override
    public String getAttributIDName() { return "idseance"; }

    @Override
    public void construirePK(Connection connexion) throws Exception {
        preparePk("SEA", "getSeqCineSeance");
        setIdseance(makePK(connexion));
    }

    @Override
    public void controler(Connection connexion) throws Exception {
        verifierAbsenceChevauchement(connexion, false);
    }

    @Override
    public void controlerUpdate(Connection connexion) throws Exception {
        verifierAbsenceChevauchement(connexion, true);
    }

    private void verifierAbsenceChevauchement(Connection connexion, boolean modification) throws Exception {
        if (connexion == null) {
            throw new IllegalArgumentException("La connexion est obligatoire pour contrôler la séance.");
        }
        if (ReglesCinema.estVide(idfilm) || ReglesCinema.estVide(idsalle)) {
            throw new IllegalArgumentException("Le film et la salle sont obligatoires.");
        }
        if (dateheure_debut == null || dateheure_fin == null) {
            throw new IllegalArgumentException("Les horaires de début et de fin sont obligatoires.");
        }
        if (!dateheure_fin.after(dateheure_debut)) {
            throw new IllegalArgumentException("La fin de la séance doit être après son début.");
        }
        if (!ReglesCinema.STATUT_PLANIFIEE.equals(statut)
                && !ReglesCinema.STATUT_OUVERTE.equals(statut)
                && !ReglesCinema.STATUT_TERMINEE.equals(statut)
                && !ReglesCinema.STATUT_ANNULEE.equals(statut)) {
            throw new IllegalArgumentException("Le statut de la séance est invalide.");
        }
        if (modification && (idseance == null || idseance.trim().isEmpty())) {
            throw new IllegalArgumentException("L'identifiant est obligatoire pour modifier une séance.");
        }

        // Sérialise les contrôles et écritures de séances visant la même salle.
        String verrouSql = "SELECT ACTIF FROM CINE_SALLE WHERE IDSALLE = ? FOR UPDATE";
        try (PreparedStatement ps = connexion.prepareStatement(verrouSql)) {
            ps.setString(1, idsalle);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Salle introuvable: " + idsalle);
                if (rs.getInt("ACTIF") != 1) throw new Exception("La salle sélectionnée est inactive.");
            }
        }

        String sql = "SELECT COUNT(*) FROM CINE_SEANCE "
                + "WHERE IDSALLE = ? AND STATUT <> '" + ReglesCinema.STATUT_ANNULEE + "' "
                + "AND DATEHEURE_DEBUT < ? AND DATEHEURE_FIN > ?"
                + (modification ? " AND IDSEANCE <> ?" : "");
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idsalle);
            ps.setTimestamp(2, dateheure_fin);
            ps.setTimestamp(3, dateheure_debut);
            if (modification) ps.setString(4, idseance);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    throw new Exception("La salle a déjà une séance planifiée sur cet intervalle.");
                }
            }
        }
    }

    public String getIdseance() { return idseance; }
    public void setIdseance(String idseance) { this.idseance = idseance; }
    public String getIdfilm() { return idfilm; }
    public void setIdfilm(String idfilm) { this.idfilm = idfilm; }
    public String getIdsalle() { return idsalle; }
    public void setIdsalle(String idsalle) { this.idsalle = idsalle; }
    public Timestamp getDateheure_debut() { return dateheure_debut; }
    public void setDateheure_debut(Timestamp dateheure_debut) { this.dateheure_debut = dateheure_debut; }
    public Timestamp getDateheure_fin() { return dateheure_fin; }
    public void setDateheure_fin(Timestamp dateheure_fin) { this.dateheure_fin = dateheure_fin; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
