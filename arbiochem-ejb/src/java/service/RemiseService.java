package service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

/** Calcul des remises configurables sans cumul. */
public final class RemiseService {

    private RemiseService() {
    }

    public static final class RemiseAppliquee {
        private final String idRemise;
        private final double taux;

        public RemiseAppliquee(String idRemise, double taux) {
            this.idRemise = idRemise;
            this.taux = taux;
        }

        public String getIdRemise() {
            return idRemise;
        }

        public double getTaux() {
            return taux;
        }
    }

    /**
     * Sélectionne une seule règle pour une catégorie cible.
     * La règle retenue est celle dont le pourcentage est le plus élevé;
     * en cas d'égalité, l'identifiant le plus petit est retenu.
     */
    public static RemiseAppliquee rechercherMeilleureRemise(Connection connexion,
            Map<String, Integer> compteCategories, String categorieCible) throws Exception {
        if (connexion == null) {
            throw new IllegalArgumentException("La connexion est obligatoire pour calculer une remise.");
        }
        if (categorieCible == null || categorieCible.trim().isEmpty()) {
            throw new IllegalArgumentException("La catégorie cible est obligatoire.");
        }

        String sql = "SELECT ID, CAT_RESERVEE, NOMBRE, VAL FROM REMISE "
                + "WHERE CAT_REMISE = ? AND CAT_RESERVEE <> CAT_REMISE ORDER BY VAL DESC, ID ASC";
        String meilleureId = null;
        double meilleurTaux = 0;

        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, categorieCible.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String categorieDeclenchante = rs.getString("CAT_RESERVEE");
                    int seuil = rs.getInt("NOMBRE");
                    int nombreAchete = compteCategories == null
                            ? 0
                            : nombrePlaces(compteCategories, categorieDeclenchante);
                    double taux = rs.getDouble("VAL");

                    if (nombreAchete >= seuil && taux > meilleurTaux) {
                        meilleureId = rs.getString("ID");
                        meilleurTaux = taux;
                    }
                }
            }
        }

        return meilleureId == null ? null : new RemiseAppliquee(meilleureId, meilleurTaux);
    }

    public static BigDecimal calculerMontantRemise(BigDecimal prixInitial,
            RemiseAppliquee remise) {
        if (prixInitial == null || prixInitial.signum() < 0) {
            throw new IllegalArgumentException("Le prix initial est invalide.");
        }
        if (remise == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (remise.getTaux() <= 0 || remise.getTaux() > 100) {
            throw new IllegalArgumentException("Le taux de remise est invalide.");
        }

        return prixInitial.multiply(BigDecimal.valueOf(remise.getTaux()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private static int nombrePlaces(Map<String, Integer> compteCategories, String categorie) {
        if (categorie == null) {
            return 0;
        }
        Integer nombre = compteCategories.get(categorie);
        return nombre == null ? 0 : nombre.intValue();
    }
}
