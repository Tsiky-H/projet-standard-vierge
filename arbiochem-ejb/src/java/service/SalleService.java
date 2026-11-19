package service;

import cinema.Place;
import cinema.Salle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import user.UserEJB;
import utilitaire.UtilDB;

/** Création atomique d'une salle et de son plan initial (une rangée par catégorie). */
public final class SalleService {

    private SalleService() { }

    public static Salle creerAvecPlan(UserEJB utilisateur, Salle salle,
            LinkedHashMap<String, Integer> nombreParCategorie) throws Exception {
        if (utilisateur == null) throw new IllegalArgumentException("La session utilisateur est obligatoire.");
        if (salle == null) throw new IllegalArgumentException("La salle est obligatoire.");
        if (nombreParCategorie == null || nombreParCategorie.isEmpty()) {
            throw new IllegalArgumentException("Le plan doit contenir au moins une catégorie de places.");
        }

        int capacite = 0;
        for (Map.Entry<String, Integer> entree : nombreParCategorie.entrySet()) {
            if (entree.getKey() == null || entree.getKey().trim().isEmpty()
                    || entree.getValue() == null || entree.getValue() < 0) {
                throw new IllegalArgumentException("Les catégories et nombres de places du plan sont invalides.");
            }
            capacite = Math.addExact(capacite, entree.getValue());
        }
        if (capacite <= 0) throw new IllegalArgumentException("Le plan doit contenir au moins une place.");
        salle.setCapacite(capacite);

        Connection connexion = null;
        try {
            connexion = new UtilDB().GetConn();
            connexion.setAutoCommit(false);
            verifierCategoriesActives(connexion, nombreParCategorie);
            utilisateur.createObject(salle, connexion);

            int indexRangee = 0;
            for (Map.Entry<String, Integer> entree : nombreParCategorie.entrySet()) {
                int nombrePlaces = entree.getValue();
                if (nombrePlaces == 0) continue;
                String rangee = nomRangee(++indexRangee);
                for (int numero = 1; numero <= nombrePlaces; numero++) {
                    Place place = new Place();
                    place.setIdsalle(salle.getTuppleID());
                    place.setRangee(rangee);
                    place.setNumero(rangee + numero);
                    place.setIdcategorieplace(entree.getKey().trim());
                    place.setActif(1);
                    utilisateur.createObject(place, connexion);
                }
            }
            connexion.commit();
            return salle;
        } catch (Exception e) {
            if (connexion != null) {
                try { connexion.rollback(); } catch (Exception rollbackError) { e.addSuppressed(rollbackError); }
            }
            throw e;
        } finally {
            if (connexion != null) try { connexion.close(); } catch (Exception ignored) { }
        }
    }

    private static void verifierCategoriesActives(Connection connexion,
            LinkedHashMap<String, Integer> nombreParCategorie) throws Exception {
        String sql = "SELECT ACTIF FROM CINE_CATEGORIE_PLACE WHERE IDCATEGORIEPLACE = ?";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            for (String idCategorie : nombreParCategorie.keySet()) {
                ps.setString(1, idCategorie.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt("ACTIF") != 1) {
                        throw new IllegalArgumentException("Une catégorie du plan est absente ou inactive.");
                    }
                }
            }
        }
    }

    private static String nomRangee(int numero) {
        StringBuilder resultat = new StringBuilder();
        while (numero > 0) {
            numero--;
            resultat.insert(0, (char) ('A' + (numero % 26)));
            numero /= 26;
        }
        return resultat.toString();
    }
}
