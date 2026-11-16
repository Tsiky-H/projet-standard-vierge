package service;

import cinema.ReglesCinema;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import utilitaire.UtilDB;

/**
 * Expiration des réservations (Ecrans-Cinema.md §9).
 *
 * Une réservation en attente dont l'échéance est dépassée, et qui n'a reçu aucun
 * paiement valide, passe à EXPIREE : ses détails et places bloquées sont libérés
 * et la vente brouillon liée est annulée. Une réservation payée à temps n'expire
 * jamais pendant l'attente de validation de la facture.
 *
 * Les changements d'état sont des UPDATE directs sur la connexion reçue : ce sont
 * des transitions d'état, pas des créations d'objets, donc elles ne passent pas
 * par controler().
 */
public class ReservationExpirationService {

    public ReservationExpirationService() { }

    /**
     * Fait expirer la réservation si elle est échue et sans paiement valide.
     * Ne valide pas la transaction : l'appelant décide du commit.
     *
     * @return true si la réservation vient de passer à EXPIREE
     */
    public boolean expirerSiNecessaire(Connection connexion, String idReservation) throws Exception {
        if (connexion == null) {
            throw new IllegalArgumentException("La connexion est obligatoire pour contrôler l'expiration.");
        }
        if (ReglesCinema.estVide(idReservation)) {
            throw new IllegalArgumentException("La réservation est obligatoire pour contrôler l'expiration.");
        }

        Timestamp expiration;
        String sql = "SELECT STATUT, DATE_EXPIRATION FROM CINE_RESERVATION WHERE IDRESERVATION = ? FOR UPDATE";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idReservation);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("Réservation introuvable : " + idReservation);
                }
                if (!ReglesCinema.STATUT_EN_ATTENTE.equals(rs.getString("STATUT"))) {
                    return false;
                }
                expiration = rs.getTimestamp("DATE_EXPIRATION");
            }
        }

        Timestamp maintenant = new Timestamp(System.currentTimeMillis());
        if (expiration != null && expiration.after(maintenant)) {
            return false;
        }
        if (existePaiementValide(connexion, idReservation)) {
            return false;
        }

        // 1. places bloquées -> EXPIREE (libère l'unicité séance/place)
        executer(connexion,
                "UPDATE CINE_RESERVATION_PLACE SET STATUT = ? WHERE STATUT = ? AND IDRESERVATION_DETAIL IN "
                + "(SELECT IDRESERVATION_DETAIL FROM CINE_RESERVATION_DETAIL WHERE IDRESERVATION = ?)",
                "EXPIREE", ReglesCinema.STATUT_BLOQUEE, idReservation);
        // 2. détails -> EXPIREE
        executer(connexion,
                "UPDATE CINE_RESERVATION_DETAIL SET STATUT = ? WHERE IDRESERVATION = ? AND STATUT = ?",
                ReglesCinema.STATUT_EXPIREE, idReservation, ReglesCinema.STATUT_EN_ATTENTE);
        // 3. lignes de la vente brouillon liée -> ANNULEE
        executer(connexion,
                "UPDATE VENTEDETAILS SET STATUT = ? WHERE STATUT = ? AND IDVENTE IN "
                + "(SELECT IDVENTE FROM VENTE WHERE IDRESERVATION = ? AND STATUT = ?)",
                ReglesCinema.STATUT_ANNULEE, ReglesCinema.STATUT_BLOQUEE,
                idReservation, ReglesCinema.STATUT_BROUILLON);
        // 4. vente brouillon -> ANNULEE
        executer(connexion,
                "UPDATE VENTE SET STATUT = ? WHERE IDRESERVATION = ? AND STATUT = ?",
                ReglesCinema.STATUT_VENTE_ANNULEE, idReservation, ReglesCinema.STATUT_BROUILLON);
        // 5. réservation -> EXPIREE
        executer(connexion,
                "UPDATE CINE_RESERVATION SET STATUT = ? WHERE IDRESERVATION = ?",
                ReglesCinema.STATUT_EXPIREE, idReservation);
        return true;
    }

    /**
     * Traitement de nettoyage (écran administratif ou tâche périodique) : chaque
     * réservation échue est traitée dans sa propre transaction.
     *
     * @return le nombre de réservations passées à EXPIREE
     */
    public int expirerReservationsEchues() throws Exception {
        List<String> identifiants = new ArrayList<>();
        try (Connection connexion = new UtilDB().GetConn();
                PreparedStatement ps = connexion.prepareStatement(
                        "SELECT IDRESERVATION FROM CINE_RESERVATION WHERE STATUT = ? AND DATE_EXPIRATION <= ?")) {
            ps.setString(1, ReglesCinema.STATUT_EN_ATTENTE);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    identifiants.add(rs.getString(1));
                }
            }
        }

        int nombre = 0;
        for (String idReservation : identifiants) {
            try (Connection connexion = new UtilDB().GetConn()) {
                connexion.setAutoCommit(false);
                try {
                    if (expirerSiNecessaire(connexion, idReservation)) {
                        nombre++;
                    }
                    connexion.commit();
                } catch (Exception erreur) {
                    try {
                        connexion.rollback();
                    } catch (Exception erreurRollback) {
                        erreur.addSuppressed(erreurRollback);
                    }
                    throw erreur;
                }
            }
        }
        return nombre;
    }

    /** Un paiement valide (donc reçu à temps, voir Paiement.controler) empêche l'expiration. */
    private static boolean existePaiementValide(Connection connexion, String idReservation) throws Exception {
        String sql = "SELECT 1 FROM CINE_PAIEMENT p JOIN VENTE v ON v.IDVENTE = p.IDVENTE "
                + "WHERE v.IDRESERVATION = ? AND p.STATUT = ? AND ROWNUM = 1";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idReservation);
            ps.setString(2, ReglesCinema.STATUT_PAIEMENT_VALIDE);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void executer(Connection connexion, String sql, String... parametres) throws Exception {
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            for (int i = 0; i < parametres.length; i++) {
                ps.setString(i + 1, parametres[i]);
            }
            ps.executeUpdate();
        }
    }
}