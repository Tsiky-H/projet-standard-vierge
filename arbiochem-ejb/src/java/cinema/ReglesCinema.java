package cinema;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Constantes et conversions communes aux contrôles métier cinéma. */
public final class ReglesCinema {

    private ReglesCinema() { }

    public static final String TYPE_VENTE_DIRECTE = "DIRECTE";
    public static final String TYPE_VENTE_RESERVATION = "RESERVATION";
    public static final String STATUT_BROUILLON = "BROUILLON";
    public static final String STATUT_VENTE_VALIDEE = "VALIDEE";
    public static final String STATUT_VENTE_ANNULEE = "ANNULEE";

    public static final String STATUT_EN_ATTENTE = "EN_ATTENTE";
    public static final String STATUT_CONFIRMEE = "CONFIRMEE";
    public static final String STATUT_EXPIREE = "EXPIREE";
    public static final String STATUT_ANNULEE = "ANNULEE";

    public static final String STATUT_PLANIFIEE = "PLANIFIEE";
    public static final String STATUT_OUVERTE = "OUVERTE";
    public static final String STATUT_TERMINEE = "TERMINEE";

    public static final String STATUT_BLOQUEE = "BLOQUEE";
    public static final String STATUT_ACHETEE = "ACHETEE";

    public static final String STATUT_PAIEMENT_VALIDE = "VALIDE";
    public static final String STATUT_PAIEMENT_REFUSE = "REFUSE";
    public static final String STATUT_PAIEMENT_ANNULE = "ANNULE";

    public static final String STATUT_MOUVEMENT_ACTIF = "ACTIF";
    public static final String STATUT_MOUVEMENT_ANNULE = "ANNULE";
    public static final String STATUT_MOUVEMENT_REMPLACE = "REMPLACE";
    public static final String TYPE_MOUVEMENT_ENCAISSEMENT = "ENCAISSEMENT";
    public static final String TYPE_MOUVEMENT_DECAISSEMENT = "DECAISSEMENT";

    public static boolean estVide(String valeur) {
        return valeur == null || valeur.trim().isEmpty();
    }

    public static BigDecimal montantDecimal(double valeur, String messageErreur) {
        if (Double.isNaN(valeur) || Double.isInfinite(valeur)) {
            throw new IllegalArgumentException(messageErreur);
        }
        return BigDecimal.valueOf(valeur).setScale(2, RoundingMode.HALF_UP);
    }
}
