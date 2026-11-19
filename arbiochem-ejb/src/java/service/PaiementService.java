package service;

import caisse.MvtCaisse;
import cinema.ReglesCinema;
import facturation.FactureClient;
import facturation.FactureClientLigne;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import paiement.LiaisonPaiement;
import paiement.Paiement;
import user.UserEJB;
import utilitaire.UtilDB;
import vente.Billet;

/**
 * Paiement d'une vente brouillon (réservation ou vente directe).
 *
 * Un paiement VALIDE crée, dans la même transaction : le paiement, la facture
 * payée avec ses lignes, la liaison paiement/facture et le mouvement
 * d'encaissement et affecte la caisse. Pour une vente directe, il la valide,
 * achète ses places et émet les billets; pour une réservation, il la confirme
 * et émet également les billets.
 */
public class PaiementService {

    public static final String ID_MODE_ESPECES = "MP000001";
    public static final String ID_MODE_MOBILE = "MP000002";
    public static final String ID_MODE_CARTE = "MP000003";

    private static final String STATUT_FACTURE_PAYEE = "PAYEE";
    private static final String SEQ_PAIEMENT = "SEQ_CINE_PAIEMENT";
    private static final String SEQ_FACTURE = "SEQ_FACTURECLIENT";

    private final ReservationExpirationService expiration = new ReservationExpirationService();

    public PaiementService() { }

    /** Résultat d'un paiement valide, pour l'affichage (monnaie à rendre en espèces). */
    public static class ResultatPaiement {
        private final Paiement paiement;
        private final FactureClient facture;
        private final double monnaieARendre;

        ResultatPaiement(Paiement paiement, FactureClient facture, double monnaieARendre) {
            this.paiement = paiement;
            this.facture = facture;
            this.monnaieARendre = monnaieARendre;
        }

        public Paiement getPaiement() { return paiement; }
        public FactureClient getFacture() { return facture; }
        public double getMonnaieARendre() { return monnaieARendre; }
    }

    /**
     * Enregistre un paiement valide pour une vente brouillon.
     *
     * @param idModePaiement mode réellement utilisé (peut différer du mode prévu)
     * @param montant        doit égaler le montant total de la vente (v1 : pas de paiement partiel)
     * @param referenceExterne obligatoire pour MOBILE et CARTE, ignorée en ESPECES
     * @param montantRecu    utilisé seulement en ESPECES pour calculer la monnaie à rendre
     * @param idCaisse       caisse qui encaisse; si vide, première caisse active
     */
    public ResultatPaiement enregistrerPaiement(UserEJB userEJB, String idVente, String idModePaiement,
            double montant, String referenceExterne, double montantRecu, String idCaisse) throws Exception {
        if (userEJB == null) {
            throw new IllegalArgumentException("Le service utilisateur est obligatoire pour enregistrer un paiement.");
        }
        if (ReglesCinema.estVide(idVente) || ReglesCinema.estVide(idModePaiement)) {
            throw new IllegalArgumentException("La vente et le mode de paiement sont obligatoires.");
        }

        try (Connection connexion = new UtilDB().GetConn()) {
            connexion.setAutoCommit(false);
            try {
                VenteInfo vente = verrouillerVente(connexion, idVente.trim());
                verifierVentePayable(connexion, vente);

                BigDecimal montantDu = vente.montantTotal;
                BigDecimal montantPaye = ReglesCinema.montantDecimal(montant, "Le montant du paiement est invalide.");
                if (montantPaye.compareTo(montantDu) != 0) {
                    throw new IllegalArgumentException(
                            "Le paiement doit couvrir exactement le montant total de la vente (" + montantDu + ").");
                }

                String idMode = lireModePaiementActif(connexion, idModePaiement.trim());
                RegleMode regle = appliquerReglesMode(connexion, idModePaiement.trim(), idMode,
                        referenceExterne, montantRecu, montantDu);

                String idCaisseUtilisee = choisirCaisse(connexion, idCaisse);

                Paiement paiement = creerPaiement(userEJB, connexion, vente, idModePaiement.trim(),
                        montantDu, regle.reference, ReglesCinema.STATUT_PAIEMENT_VALIDE);
                FactureClient facture = creerFacturePayee(userEJB, connexion, vente);
                creerLiaison(userEJB, connexion, paiement, facture, montantDu);
                creerMouvementEncaissement(userEJB, connexion, paiement, facture, idCaisseUtilisee, montantDu, idMode);
                affecterCaisse(connexion, vente.idVente, idCaisseUtilisee);
                finaliserVenteEtEmettreBillets(
                        userEJB, connexion, vente, facture, paiement);

                connexion.commit();
                return new ResultatPaiement(paiement, facture, regle.monnaieARendre);
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

    /**
     * Trace une tentative refusée (carte refusée, mobile non reçu...). Elle n'a aucun autre
     * effet : pas de facture ni de mouvement, la vente reste brouillon et la réservation en
     * attente jusqu'à son expiration.
     */
    public Paiement enregistrerPaiementRefuse(UserEJB userEJB, String idVente, String idModePaiement,
            String referenceExterne) throws Exception {
        if (userEJB == null) {
            throw new IllegalArgumentException("Le service utilisateur est obligatoire pour enregistrer un paiement.");
        }
        if (ReglesCinema.estVide(idVente) || ReglesCinema.estVide(idModePaiement)) {
            throw new IllegalArgumentException("La vente et le mode de paiement sont obligatoires.");
        }

        try (Connection connexion = new UtilDB().GetConn()) {
            connexion.setAutoCommit(false);
            try {
                VenteInfo vente = verrouillerVente(connexion, idVente.trim());
                verifierVentePayable(connexion, vente);
                String idMode = lireModePaiementActif(connexion, idModePaiement.trim());
                verifierModePaiementPrisEnCharge(idMode);

                String reference = referenceExterne == null ? null : referenceExterne.trim();
                if (reference != null && reference.isEmpty()) {
                    reference = null;
                }
                Paiement paiement = creerPaiement(userEJB, connexion, vente, idModePaiement.trim(),
                        vente.montantTotal, reference, ReglesCinema.STATUT_PAIEMENT_REFUSE);
                connexion.commit();
                return paiement;
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

    // ------------------------------------------------------------------
    // Contrôles
    // ------------------------------------------------------------------

    private static class VenteInfo {
        String idVente;
        String statut;
        String typeVente;
        String idClient;
        String idReservation;
        BigDecimal montantTotal;
    }

    private static class RegleMode {
        String reference;
        double monnaieARendre;
    }

    /** Verrouille la vente : deux paiements simultanés sur la même vente s'exécutent l'un après l'autre. */
    private static VenteInfo verrouillerVente(Connection connexion, String idVente) throws Exception {
        String sql = "SELECT STATUT, TYPE_VENTE, IDCLIENT, IDRESERVATION, MONTANT_TOTAL "
                + "FROM VENTE WHERE IDVENTE = ? FOR UPDATE";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idVente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("Vente introuvable : " + idVente);
                }
                VenteInfo vente = new VenteInfo();
                vente.idVente = idVente;
                vente.statut = rs.getString("STATUT");
                vente.typeVente = rs.getString("TYPE_VENTE");
                vente.idClient = rs.getString("IDCLIENT");
                vente.idReservation = rs.getString("IDRESERVATION");
                BigDecimal total = rs.getBigDecimal("MONTANT_TOTAL");
                vente.montantTotal = total == null ? BigDecimal.ZERO : total.setScale(2, RoundingMode.HALF_UP);
                return vente;
            }
        }
    }

    /** Vente brouillon, non expirée, sans paiement valide et avec un montant à payer. */
    private void verifierVentePayable(Connection connexion, VenteInfo vente) throws Exception {
        if (!ReglesCinema.STATUT_BROUILLON.equals(vente.statut)) {
            throw new Exception("Cette vente n'est plus payable (statut " + vente.statut + ").");
        }
        if (vente.montantTotal.signum() <= 0) {
            throw new Exception("La vente n'a aucun montant à payer.");
        }

        if (!ReglesCinema.estVide(vente.idReservation)) {
            // Contrôle avant paiement (Ecrans-Cinema.md §9). Si la réservation vient d'expirer, on
            // valide l'expiration AVANT de refuser : sinon le rollback annulerait la libération des places.
            if (expiration.expirerSiNecessaire(connexion, vente.idReservation)) {
                connexion.commit();
                throw new Exception("La réservation a expiré : les places ont été libérées et le paiement est refusé.");
            }
        }

        String sql = "SELECT 1 FROM CINE_PAIEMENT WHERE IDVENTE = ? AND STATUT = ? AND ROWNUM = 1";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, vente.idVente);
            ps.setString(2, ReglesCinema.STATUT_PAIEMENT_VALIDE);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new Exception("Un paiement valide existe déjà pour cette vente.");
                }
            }
        }
    }

    private static String lireModePaiementActif(Connection connexion, String idModePaiement) throws Exception {
        String sql = "SELECT IDMODEPAIEMENT, ACTIF FROM MODEPAIEMENT WHERE IDMODEPAIEMENT = ?";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idModePaiement);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("Le mode de paiement sélectionné est introuvable.");
                }
                if (rs.getInt("ACTIF") != 1) {
                    throw new Exception("Le mode de paiement sélectionné est inactif.");
                }
                return rs.getString("IDMODEPAIEMENT");
            }
        }
    }

    /** Ce qui change selon le mode : espèces (monnaie), mobile et carte (référence obligatoire et unique). */
    private static RegleMode appliquerReglesMode(Connection connexion, String idModePaiement, String idMode,
            String referenceExterne, double montantRecu, BigDecimal montantDu) throws Exception {
        RegleMode regle = new RegleMode();
        String reference = referenceExterne == null ? null : referenceExterne.trim();
        if (reference != null && reference.isEmpty()) {
            reference = null;
        }

        if (ID_MODE_ESPECES.equals(idMode)) {
            BigDecimal recu = ReglesCinema.montantDecimal(montantRecu, "Le montant reçu est invalide.");
            if (recu.compareTo(montantDu) < 0) {
                throw new IllegalArgumentException("Le montant reçu est inférieur au montant à payer.");
            }
            regle.monnaieARendre = recu.subtract(montantDu).doubleValue();
            reference = null;
        } else if (ID_MODE_MOBILE.equals(idMode) || ID_MODE_CARTE.equals(idMode)) {
            if (reference == null) {
                throw new IllegalArgumentException(
                        "La référence de la transaction est obligatoire pour ce mode de paiement.");
            }
            verifierReferenceInedite(connexion, idModePaiement, reference);
        } else verifierModePaiementPrisEnCharge(idMode);

        if (reference != null && reference.length() > 120) {
            throw new IllegalArgumentException("La référence ne peut pas dépasser 120 caractères.");
        }
        regle.reference = reference;
        return regle;
    }

    private static void verifierModePaiementPrisEnCharge(String idMode) {
        if (!ID_MODE_ESPECES.equals(idMode)
                && !ID_MODE_MOBILE.equals(idMode)
                && !ID_MODE_CARTE.equals(idMode)) {
            throw new IllegalArgumentException("Ce mode de paiement n'est pas pris en charge.");
        }
    }

    /** Une même référence de transaction ne peut pas encaisser deux ventes. */
    private static void verifierReferenceInedite(Connection connexion, String idModePaiement,
            String reference) throws Exception {
        String sql = "SELECT 1 FROM CINE_PAIEMENT WHERE IDMODEPAIEMENT = ? AND UPPER(REFERENCE_EXTERNE) = UPPER(?) "
                + "AND STATUT = ? AND ROWNUM = 1";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idModePaiement);
            ps.setString(2, reference);
            ps.setString(3, ReglesCinema.STATUT_PAIEMENT_VALIDE);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new Exception("Cette référence de transaction a déjà été utilisée.");
                }
            }
        }
    }

    /** Caisse demandée si elle est active, sinon première caisse active. */
    private static String choisirCaisse(Connection connexion, String idCaisse) throws Exception {
        if (!ReglesCinema.estVide(idCaisse)) {
            try (PreparedStatement ps = connexion.prepareStatement("SELECT ACTIF FROM CAISSE WHERE IDCAISSE = ?")) {
                ps.setString(1, idCaisse.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new Exception("La caisse sélectionnée est introuvable.");
                    }
                    if (rs.getInt(1) != 1) {
                        throw new Exception("La caisse sélectionnée est inactive.");
                    }
                    return idCaisse.trim();
                }
            }
        }
        String sql = "SELECT IDCAISSE FROM CAISSE WHERE ACTIF = 1 ORDER BY IDCAISSE";
        try (PreparedStatement ps = connexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new Exception("Aucune caisse active n'est disponible pour encaisser.");
            }
            return rs.getString(1);
        }
    }

    // ------------------------------------------------------------------
    // Créations (toutes sur la connexion de la transaction)
    // ------------------------------------------------------------------

    private static Paiement creerPaiement(UserEJB userEJB, Connection connexion, VenteInfo vente,
            String idModePaiement, BigDecimal montant, String reference, String statut) throws Exception {
        Paiement paiement = new Paiement();
        paiement.setNumero_paiement(prochainNumero(connexion, SEQ_PAIEMENT, "PAI"));
        paiement.setDaty(new Timestamp(System.currentTimeMillis()));
        paiement.setIdmodepaiement(idModePaiement);
        paiement.setStatut(statut);
        paiement.setMontant(montant.doubleValue());
        paiement.setReference_externe(reference);
        paiement.setIdvente(vente.idVente);
        Object cree = userEJB.createObject(paiement, connexion);
        if (!(cree instanceof Paiement)) {
            throw new Exception("Le framework n'a pas retourné le paiement créé.");
        }
        return (Paiement) cree;
    }

    /** Une ligne facturable : soit une place de réservation, soit une ligne de vente directe. */
    private static class LigneFacturable {
        String idReservationPlace;
        String idVenteDetails;
        String designation;
        BigDecimal quantite;
        BigDecimal prixUnitaire;
        BigDecimal montant;
    }

    private static FactureClient creerFacturePayee(UserEJB userEJB, Connection connexion,
            VenteInfo vente) throws Exception {
        List<LigneFacturable> lignes = lireLignesFacturables(connexion, vente);
        if (lignes.isEmpty()) {
            throw new Exception("La vente ne contient aucune ligne à facturer.");
        }
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (LigneFacturable ligne : lignes) {
            total = total.add(ligne.montant);
        }
        if (total.compareTo(vente.montantTotal) != 0) {
            throw new Exception("Le total des lignes (" + total + ") ne correspond pas au montant de la vente ("
                    + vente.montantTotal + ").");
        }

        // Pas de TVA gérée en v1 : HT = TTC, taux 0 (contrainte SQL TTC = HT + TVA).
        FactureClient facture = new FactureClient();
        facture.setNumero_facture(prochainNumero(connexion, SEQ_FACTURE, "FAC"));
        facture.setIdclient(vente.idClient);
        facture.setDaty(new java.sql.Date(System.currentTimeMillis()));
        facture.setStatut(STATUT_FACTURE_PAYEE);
        facture.setMontant_ht(total.doubleValue());
        facture.setMontant_tva(0);
        facture.setMontant_ttc(total.doubleValue());
        Object factureCreee = userEJB.createObject(facture, connexion);
        if (!(factureCreee instanceof FactureClient)) {
            throw new Exception("Le framework n'a pas retourné la facture créée.");
        }
        FactureClient resultat = (FactureClient) factureCreee;

        for (LigneFacturable source : lignes) {
            FactureClientLigne ligne = new FactureClientLigne();
            ligne.setIdfactureclient(resultat.getIdfactureclient());
            ligne.setDesignation(source.designation);
            ligne.setQuantite(source.quantite.doubleValue());
            ligne.setPrix_unitaire(source.prixUnitaire.doubleValue());
            ligne.setTaux_tva(0);
            ligne.setMontant_ht(source.montant.doubleValue());
            ligne.setMontant_tva(0);
            ligne.setMontant_ttc(source.montant.doubleValue());
            // Contrainte SQL : exactement une source par ligne.
            ligne.setIdreservation_place(source.idReservationPlace);
            ligne.setIdventedetails(source.idVenteDetails);
            userEJB.createObject(ligne, connexion);
        }
        return resultat;
    }

    private static List<LigneFacturable> lireLignesFacturables(Connection connexion,
            VenteInfo vente) throws Exception {
        List<LigneFacturable> lignes = new ArrayList<>();
        if (ReglesCinema.TYPE_VENTE_RESERVATION.equals(vente.typeVente)) {
            String sql = "SELECT rp.IDRESERVATION_PLACE, rp.PRIX_UNITAIRE, p.RANGEE, p.NUMERO "
                    + "FROM CINE_RESERVATION_PLACE rp "
                    + "JOIN CINE_RESERVATION_DETAIL rd ON rd.IDRESERVATION_DETAIL = rp.IDRESERVATION_DETAIL "
                    + "JOIN CINE_PLACE p ON p.IDPLACE = rp.IDPLACE "
                    + "WHERE rd.IDRESERVATION = ? AND rp.STATUT = ? ORDER BY rp.IDSEANCE, p.RANGEE, p.NUMERO";
            try (PreparedStatement ps = connexion.prepareStatement(sql)) {
                ps.setString(1, vente.idReservation);
                ps.setString(2, ReglesCinema.STATUT_BLOQUEE);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        LigneFacturable ligne = new LigneFacturable();
                        ligne.idReservationPlace = rs.getString("IDRESERVATION_PLACE");
                        ligne.designation = "Billet - place " + rs.getString("RANGEE") + rs.getString("NUMERO");
                        ligne.quantite = BigDecimal.ONE.setScale(2);
                        ligne.prixUnitaire = rs.getBigDecimal("PRIX_UNITAIRE").setScale(2, RoundingMode.HALF_UP);
                        ligne.montant = ligne.prixUnitaire;
                        lignes.add(ligne);
                    }
                }
            }
        } else {
            String sql = "SELECT IDVENTEDETAILS, DESIGNATION, QUANTITE, PRIX_UNITAIRE, MONTANT_LIGNE "
                    + "FROM VENTEDETAILS WHERE IDVENTE = ? AND STATUT = ? ORDER BY IDVENTEDETAILS";
            try (PreparedStatement ps = connexion.prepareStatement(sql)) {
                ps.setString(1, vente.idVente);
                ps.setString(2, ReglesCinema.STATUT_BLOQUEE);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        LigneFacturable ligne = new LigneFacturable();
                        ligne.idVenteDetails = rs.getString("IDVENTEDETAILS");
                        ligne.designation = rs.getString("DESIGNATION");
                        ligne.quantite = rs.getBigDecimal("QUANTITE").setScale(2, RoundingMode.HALF_UP);
                        ligne.prixUnitaire = rs.getBigDecimal("PRIX_UNITAIRE").setScale(2, RoundingMode.HALF_UP);
                        ligne.montant = rs.getBigDecimal("MONTANT_LIGNE").setScale(2, RoundingMode.HALF_UP);
                        lignes.add(ligne);
                    }
                }
            }
        }
        return lignes;
    }

    private static void creerLiaison(UserEJB userEJB, Connection connexion, Paiement paiement,
            FactureClient facture, BigDecimal montant) throws Exception {
        LiaisonPaiement liaison = new LiaisonPaiement();
        liaison.setIdpaiement(paiement.getIdpaiement());
        liaison.setIdfactureclient(facture.getIdfactureclient());
        liaison.setMontant_affecte(montant.doubleValue());
        liaison.setDaty(new java.sql.Date(System.currentTimeMillis()));
        userEJB.createObject(liaison, connexion);
    }

    /**
     * Mouvement créé non validé : sa validation APJ est un contrôle de caisse séparé (Flux D).
     * La date est posée ici et pas dans construirePK : CreateObject appelle controler avant
     * construirePK, donc une valeur par défaut posée dans la PK arriverait trop tard.
     */
    private static void creerMouvementEncaissement(UserEJB userEJB, Connection connexion, Paiement paiement,
            FactureClient facture, String idCaisse, BigDecimal montant, String idMode) throws Exception {
        // Le paiement peut avoir eu sa date perdue (objet rechargé) : la date du mouvement ne doit jamais être nulle.
        java.sql.Timestamp datePaiement = paiement.getDaty() != null
                ? paiement.getDaty() : new java.sql.Timestamp(System.currentTimeMillis());
        MvtCaisse mouvement = new MvtCaisse();
        mouvement.setIdcaisse(idCaisse);
        mouvement.setType_mouvement(ReglesCinema.TYPE_MOUVEMENT_ENCAISSEMENT);
        mouvement.setMontant(montant.doubleValue());
        mouvement.setIdpaiement(paiement.getIdpaiement());
        mouvement.setIdfactureclient(facture.getIdfactureclient());
        mouvement.setDaty(new java.sql.Date(datePaiement.getTime()));
        mouvement.setObservation("Encaissement du paiement " + paiement.getNumero_paiement() + " (" + idMode + ")");
        userEJB.createObject(mouvement, connexion);
    }

    /** UPDATE direct : Vente.controler ne sert qu'à la création d'une vente brouillon. */
    private static void affecterCaisse(Connection connexion, String idVente, String idCaisse) throws Exception {
        String sql = "UPDATE VENTE SET IDCAISSE = ? WHERE IDVENTE = ? AND STATUT = ?";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, idCaisse);
            ps.setString(2, idVente);
            ps.setString(3, ReglesCinema.STATUT_BROUILLON);
            if (ps.executeUpdate() != 1) {
                throw new Exception("La caisse n'a pas pu être affectée à la vente.");
            }
        }
    }

    /**
     * Le paiement finalise immédiatement la vente ou la réservation et émet
     * un billet par place. Le visa APJ de la facture reste une validation
     * documentaire et ne retarde pas la remise des billets.
     */
    private static void finaliserVenteEtEmettreBillets(UserEJB userEJB,
                    Connection connexion, VenteInfo vente, FactureClient facture, Paiement paiement) throws Exception {
                if (ReglesCinema.TYPE_VENTE_DIRECTE.equals(vente.typeVente)) {
                    finaliserVenteDirecteEtEmettreBillets(userEJB, connexion, vente, facture, paiement);
                    return;
                }
                if (!ReglesCinema.TYPE_VENTE_RESERVATION.equals(vente.typeVente)) return;
                if (ReglesCinema.estVide(vente.idReservation)) {
                    throw new Exception("La vente de réservation ne référence aucune réservation.");
                }

                update(connexion,
                        "UPDATE VENTE SET STATUT = ?, IDFACTURECLIENT = ? "
                                + "WHERE IDVENTE = ? AND STATUT = ?",
                        ReglesCinema.STATUT_VENTE_VALIDEE, facture.getIdfactureclient(),
                        vente.idVente, ReglesCinema.STATUT_BROUILLON);
                update(connexion,
                        "UPDATE CINE_RESERVATION SET STATUT = ? "
                                + "WHERE IDRESERVATION = ? AND STATUT = ?",
                        ReglesCinema.STATUT_CONFIRMEE, vente.idReservation, ReglesCinema.STATUT_EN_ATTENTE);
                update(connexion,
                        "UPDATE CINE_RESERVATION_DETAIL SET STATUT = ? "
                                + "WHERE IDRESERVATION = ? AND STATUT = ?",
                        ReglesCinema.STATUT_CONFIRMEE, vente.idReservation, ReglesCinema.STATUT_EN_ATTENTE);
                update(connexion,
                        "UPDATE CINE_RESERVATION_PLACE SET STATUT = ?, DATE_VALIDATION = SYSDATE "
                                + "WHERE STATUT = ? AND IDRESERVATION_DETAIL IN "
                                + "(SELECT IDRESERVATION_DETAIL FROM CINE_RESERVATION_DETAIL WHERE IDRESERVATION = ?)",
                        ReglesCinema.STATUT_ACHETEE, ReglesCinema.STATUT_BLOQUEE, vente.idReservation);
                update(connexion,
                        "UPDATE VENTEDETAILS SET STATUT = ? WHERE IDVENTE = ? AND STATUT = ?",
                        ReglesCinema.STATUT_ACHETEE, vente.idVente, ReglesCinema.STATUT_BLOQUEE);

                String sql = "SELECT IDRESERVATION_PLACE FROM CINE_RESERVATION_PLACE "
                        + "WHERE STATUT = ? AND IDRESERVATION_DETAIL IN "
                        + "(SELECT IDRESERVATION_DETAIL FROM CINE_RESERVATION_DETAIL WHERE IDRESERVATION = ?) "
                        + "ORDER BY IDRESERVATION_PLACE";
                try (PreparedStatement ps = connexion.prepareStatement(sql)) {
                    ps.setString(1, ReglesCinema.STATUT_ACHETEE);
                    ps.setString(2, vente.idReservation);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String idPlaceReservation = rs.getString(1);
                            Billet billet = new Billet();
                            billet.setNumero_billet(prochainNumero(connexion, "SEQ_CINE_BILLET", "BIL"));
                            billet.setDaty_emission(new java.sql.Date(paiement.getDaty().getTime()));
                            billet.setStatut("EMIS");
                            billet.setIdreservation_place(idPlaceReservation);
                            billet.setQr_payload("BILLET:" + billet.getNumero_billet()
                                    + "|RESERVATION:" + vente.idReservation
                                    + "|PAIEMENT:" + paiement.getNumero_paiement());
                            userEJB.createObject(billet, connexion);
                        }
                    }
                }
            }

    private static void finaliserVenteDirecteEtEmettreBillets(UserEJB userEJB, Connection connexion,
            VenteInfo vente, FactureClient facture, Paiement paiement) throws Exception {
        update(connexion,
                "UPDATE VENTE SET STATUT = ?, IDFACTURECLIENT = ? WHERE IDVENTE = ? AND TYPE_VENTE = ? AND STATUT = ?",
                ReglesCinema.STATUT_VENTE_VALIDEE, facture.getIdfactureclient(), vente.idVente,
                ReglesCinema.TYPE_VENTE_DIRECTE, ReglesCinema.STATUT_BROUILLON);
        update(connexion,
                "UPDATE VENTEDETAILS SET STATUT = ? WHERE IDVENTE = ? AND STATUT = ?",
                ReglesCinema.STATUT_ACHETEE, vente.idVente, ReglesCinema.STATUT_BLOQUEE);

        String sql = "SELECT IDVENTEDETAILS FROM VENTEDETAILS WHERE IDVENTE = ? AND STATUT = ? ORDER BY IDVENTEDETAILS";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, vente.idVente);
            ps.setString(2, ReglesCinema.STATUT_ACHETEE);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String idDetail = rs.getString("IDVENTEDETAILS");
                    Billet billet = new Billet();
                    billet.setNumero_billet(prochainNumero(connexion, "SEQ_CINE_BILLET", "BIL"));
                    billet.setDaty_emission(new java.sql.Date(paiement.getDaty().getTime()));
                    billet.setStatut("EMIS");
                    billet.setIdventedetails(idDetail);
                    billet.setQr_payload("BILLET:" + billet.getNumero_billet()
                            + "|VENTE:" + vente.idVente + "|PAIEMENT:" + paiement.getNumero_paiement());
                    userEJB.createObject(billet, connexion);
                }
            }
        }
    }

    private static void update(Connection connexion, String sql, String... parametres) throws Exception {
                try (PreparedStatement ps = connexion.prepareStatement(sql)) {
                    for (int i = 0; i < parametres.length; i++) {
                        ps.setString(i + 1, parametres[i]);
                    }
                    if (ps.executeUpdate() == 0) {
                        throw new Exception("Transition métier impossible : aucune ligne concernée.");
                }
            }
        }

    /** Numéro lisible et unique (NUMERO_PAIEMENT et NUMERO_FACTURE sont NOT NULL UNIQUE). */
    private static String prochainNumero(Connection connexion, String sequence, String prefixe) throws Exception {
        // Le nom de séquence vient des constantes de cette classe, jamais d'une saisie.
        try (PreparedStatement ps = connexion.prepareStatement("SELECT " + sequence + ".NEXTVAL FROM DUAL");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            String annee = String.valueOf(new java.util.GregorianCalendar().get(java.util.Calendar.YEAR));
            return String.format("%s-%s-%06d", prefixe, annee, rs.getLong(1));
        }
    }
}
