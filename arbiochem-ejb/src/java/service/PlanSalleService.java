package service;

import bean.CGenUtil;
import cinema.PlaceSeanceDispo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Comparator;
import utilitaire.UtilDB;

/** Lecture du plan de places d'une séance et calcul de l'échéance d'une réservation. */
public final class PlanSalleService {

    private PlanSalleService() { }

    /** Ordre d'affichage : rangée, puis numéro (A2 avant A10). */
    private static final Comparator<PlaceSeanceDispo> ORDRE_PLAN = (a, b) -> {
        int parRangee = texte(a.getRangee()).compareTo(texte(b.getRangee()));
        if (parRangee != 0) return parRangee;
        String na = texte(a.getNumero());
        String nb = texte(b.getNumero());
        int parLongueur = Integer.compare(na.length(), nb.length());
        return parLongueur != 0 ? parLongueur : na.compareTo(nb);
    };

    /**
     * Toutes les places de la salle de la séance (libres ou non), triées pour
     * dessiner le plan. L'état d'occupation vient de la vue CINE_PLACE_SEANCE_DISPO.
     */
    public static PlaceSeanceDispo[] listerPlacesSeance(String idSeance) throws Exception {
        if (idSeance == null || idSeance.trim().isEmpty()) {
            throw new IllegalArgumentException("La séance est obligatoire.");
        }
        String valeur = idSeance.trim().replace("'", "''");
        try (Connection connexion = new UtilDB().GetConn()) {
            Object[] resultats = CGenUtil.rechercher(new PlaceSeanceDispo(), null, null,
                    connexion, " AND IDSEANCE = '" + valeur + "'");
            PlaceSeanceDispo[] places = new PlaceSeanceDispo[resultats.length];
            System.arraycopy(resultats, 0, places, 0, resultats.length);
            Arrays.sort(places, ORDRE_PLAN);
            return places;
        }
    }

    /** Échéance = maintenant + délai, sans jamais dépasser le début de la séance. */
    public static Timestamp calculerEcheance(String idSeance, int minutes) throws Exception {
        if (idSeance == null || idSeance.trim().isEmpty()) {
            throw new IllegalArgumentException("La séance est obligatoire.");
        }
        Timestamp limite = new Timestamp(System.currentTimeMillis() + minutes * 60_000L);
        String sql = "SELECT DATEHEURE_DEBUT FROM CINE_SEANCE WHERE IDSEANCE = ?";
        try (Connection connexion = new UtilDB().GetConn();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idSeance.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Séance introuvable : " + idSeance);
                Timestamp debut = rs.getTimestamp(1);
                return (debut != null && debut.before(limite)) ? debut : limite;
            }
        }
    }

    private static String texte(String valeur) {
        return valeur == null ? "" : valeur;
    }
}