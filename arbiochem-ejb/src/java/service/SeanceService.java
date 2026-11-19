package service;

import cinema.ReglesCinema;
import cinema.Seance;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import user.UserEJB;
import utilitaire.UtilDB;

/** Création d'une séance dont l'heure de fin dépend de la durée du film. */
public final class SeanceService {

    private SeanceService() { }

    public static Seance creer(UserEJB utilisateur, Seance seance) throws Exception {
        if (utilisateur == null) throw new IllegalArgumentException("La session utilisateur est obligatoire.");
        if (seance == null || ReglesCinema.estVide(seance.getIdfilm())
                || ReglesCinema.estVide(seance.getIdsalle()) || seance.getDateheure_debut() == null) {
            throw new IllegalArgumentException("Le film, la salle et le début de séance sont obligatoires.");
        }

        Connection connexion = null;
        try {
            connexion = new UtilDB().GetConn();
            connexion.setAutoCommit(false);
            int duree = lireDureeFilmActif(connexion, seance.getIdfilm().trim());
            seance.setDateheure_fin(new Timestamp(seance.getDateheure_debut().getTime() + duree * 60_000L));
            seance.setStatut(ReglesCinema.STATUT_OUVERTE);
            seance.setNomTable("CINE_SEANCE");
            utilisateur.createObject(seance, connexion);
            connexion.commit();
            return seance;
        } catch (Exception e) {
            if (connexion != null) {
                try { connexion.rollback(); } catch (Exception rollbackError) { e.addSuppressed(rollbackError); }
            }
            throw e;
        } finally {
            if (connexion != null) try { connexion.close(); } catch (Exception ignored) { }
        }
    }

    private static int lireDureeFilmActif(Connection connexion, String idFilm) throws Exception {
        String sql = "SELECT DUREE_MINUTES FROM CINE_FILM WHERE IDFILM = ? AND ACTIF = 1";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idFilm);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Le film sélectionné est absent ou inactif.");
                int duree = rs.getInt("DUREE_MINUTES");
                if (duree <= 0) throw new IllegalArgumentException("La durée du film doit être positive.");
                return duree;
            }
        }
    }
}
