-- ============================================================================
-- DONNEES INITIALES CINEMA (Oracle / SQL*Plus)
-- A executer apres cinema-application-final.sql.
-- Pour recharger les donnees, executer d'abord cleanup-cinema-data.sql.
--
-- Programme conforme au PDF "Donnees CINEMA S5"
-- Periode : du 16/11/2026 au 30/11/2026
--
-- Remarque :
-- La table CINE_FILM utilise actuellement les colonnes
-- IDFILM, TITRE, DUREE_MINUTES, CLASSIFICATION_AGE et ACTIF.
-- Les realisateurs et les genres du PDF ne sont donc pas enregistres ici.
-- Classification : Tous publics = 0 ; -12 = 12.
-- ============================================================================

SET DEFINE OFF
SET SQLBLANKLINES ON
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

-- ============================================================================
-- 1) MODES DE PAIEMENT
-- ============================================================================

INSERT INTO MODEPAIEMENT (IDMODEPAIEMENT, LIBELLE, ACTIF)
VALUES ('MP000001', 'Especes', 1);

INSERT INTO MODEPAIEMENT (IDMODEPAIEMENT, LIBELLE, ACTIF)
VALUES ('MP000002', 'Mobile money', 1);

INSERT INTO MODEPAIEMENT (IDMODEPAIEMENT, LIBELLE, ACTIF)
VALUES ('MP000003', 'Carte bancaire', 1);


-- ============================================================================
-- 2) CAISSE
-- ============================================================================

INSERT INTO CAISSE (IDCAISSE, LIBELLE, SOLDE_THEORIQUE, ACTIF)
VALUES ('CAI000001', 'Caisse principale', 0, 1);


-- ============================================================================
-- 3) CATEGORIES DE PLACES
-- PDF : Standard A-B = 15 000 Ar ; Confort C = 20 000 Ar ;
--       VIP D = 30 000 Ar.
-- ============================================================================

INSERT INTO CINE_CATEGORIE_PLACE
(IDCATEGORIEPLACE, LIBELLE, PRIX_BASE, ACTIF)
VALUES ('CAT000001', 'Standard', 15000, 1);

INSERT INTO CINE_CATEGORIE_PLACE
(IDCATEGORIEPLACE, LIBELLE, PRIX_BASE, ACTIF)
VALUES ('CAT000002', 'Confort', 20000, 1);

INSERT INTO CINE_CATEGORIE_PLACE
(IDCATEGORIEPLACE, LIBELLE, PRIX_BASE, ACTIF)
VALUES ('CAT000003', 'VIP', 30000, 1);


-- ============================================================================
-- REGLES DE REMISE CONFIGUREES
-- NOMBRE = seuil minimum d'achat dans CAT_RESERVEE.
-- VAL = pourcentage applique individuellement a chaque place de CAT_REMISE.
-- Le choix de la meilleure règle et la non-cumulabilite sont geres en Java.
-- ============================================================================

-- Au moins 4 places Standard : 50% sur chaque place Confort.
INSERT INTO REMISE (ID, CAT_RESERVEE, NOMBRE, VAL, CAT_REMISE)
VALUES ('REM000001', 'CAT000001', 4, 50, 'CAT000002');

-- Au moins 2 places Confort : 75% sur chaque place VIP.
INSERT INTO REMISE (ID, CAT_RESERVEE, NOMBRE, VAL, CAT_REMISE)
VALUES ('REM000002', 'CAT000002', 2, 75, 'CAT000003');



-- ============================================================================
-- 4) CLIENTS DE DEMONSTRATION
-- ============================================================================

INSERT INTO CLIENT (IDCLIENT, NOM, PRENOM, EMAIL, TELEPHONE, ACTIF)
VALUES ('CLI000001', 'Rasoanaivo', 'Mika',
        'mika.rasoanaivo@example.test', '0340000001', 1);

INSERT INTO CLIENT (IDCLIENT, NOM, PRENOM, EMAIL, TELEPHONE, ACTIF)
VALUES ('CLI000002', 'Rakoto', 'Aina',
        'aina.rakoto@example.test', '0340000002', 1);

INSERT INTO CLIENT (IDCLIENT, NOM, PRENOM, EMAIL, TELEPHONE, ACTIF)
VALUES ('CLI000003', 'Andriamihaja', 'Tojo',
        'tojo.andriamihaja@example.test', '0340000003', 1);


-- ============================================================================
-- 5) SALLES
-- PDF : trois salles de 20 places chacune.
-- ============================================================================

INSERT INTO CINE_SALLE (IDSALLE, LIBELLE, CAPACITE, ACTIF)
VALUES ('SAL000001', 'Salle A', 20, 1);

INSERT INTO CINE_SALLE (IDSALLE, LIBELLE, CAPACITE, ACTIF)
VALUES ('SAL000002', 'Salle B', 20, 1);

INSERT INTO CINE_SALLE (IDSALLE, LIBELLE, CAPACITE, ACTIF)
VALUES ('SAL000003', 'Salle C', 20, 1);


-- ============================================================================
-- 6) PLACES
-- Chaque salle contient :
-- A1-A5 et B1-B5 : Standard
-- C1-C5          : Confort
-- D1-D5          : VIP
-- Total : 20 places par salle, 60 places.
-- ============================================================================

DECLARE
    v_numero_global NUMBER := 0;
    v_rangee        VARCHAR2(1);
    v_categorie     VARCHAR2(10);
BEGIN
    FOR s IN (
        SELECT 'SAL000001' AS IDSALLE FROM DUAL
        UNION ALL
        SELECT 'SAL000002' AS IDSALLE FROM DUAL
        UNION ALL
        SELECT 'SAL000003' AS IDSALLE FROM DUAL
    ) LOOP
        FOR r IN 1..4 LOOP
            v_rangee := CHR(64 + r);

            IF r IN (1, 2) THEN
                v_categorie := 'CAT000001';
            ELSIF r = 3 THEN
                v_categorie := 'CAT000002';
            ELSE
                v_categorie := 'CAT000003';
            END IF;

            FOR n IN 1..5 LOOP
                v_numero_global := v_numero_global + 1;

                INSERT INTO CINE_PLACE
                    (IDPLACE, IDSALLE, NUMERO, RANGEE,
                     IDCATEGORIEPLACE, ACTIF)
                VALUES
                    ('PLC' || LPAD(v_numero_global, 6, '0'),
                     s.IDSALLE,
                     v_rangee || TO_CHAR(n),
                     v_rangee,
                     v_categorie,
                     1);
            END LOOP;
        END LOOP;
    END LOOP;
END;
/


-- ============================================================================
-- 7) FILMS
-- Les realisateurs et genres ne sont pas ajoutes car les colonnes
-- correspondantes ne figurent pas dans la structure actuelle de CINE_FILM.
-- ============================================================================

INSERT INTO CINE_FILM
    (IDFILM, TITRE, DUREE_MINUTES, CLASSIFICATION_AGE, ACTIF)
VALUES
    ('FIL000001', 'Les Gardiens du Baobab', 115, 0, 1);

INSERT INTO CINE_FILM
    (IDFILM, TITRE, DUREE_MINUTES, CLASSIFICATION_AGE, ACTIF)
VALUES
    ('FIL000002', 'Nuit sur Analamanga', 105, 12, 1);

INSERT INTO CINE_FILM
    (IDFILM, TITRE, DUREE_MINUTES, CLASSIFICATION_AGE, ACTIF)
VALUES
    ('FIL000003', 'Le Petit Lémurien', 90, 0, 1);


-- ============================================================================
-- 8) SEANCES
-- Dates du programme : du 16/11/2026 au 30/11/2026.
-- Les heures de fin sont calculees automatiquement selon la duree du film.
-- Le programme hebdomadaire est applique sur toute la periode,
-- y compris le lundi 30/11/2026.
-- Total attendu : 41 seances.
-- ============================================================================

DECLARE
    v_numero_seance NUMBER := 0;

    PROCEDURE ajouter_seance(
        p_film       IN VARCHAR2,
        p_salle      IN VARCHAR2,
        p_debut      IN VARCHAR2,
        p_duree      IN NUMBER
    )
    IS
        v_debut DATE;
    BEGIN
        v_numero_seance := v_numero_seance + 1;

        v_debut := TO_DATE(p_debut, 'DD/MM/YYYY HH24:MI');

        INSERT INTO CINE_SEANCE
            (IDSEANCE, IDFILM, IDSALLE,
             DATEHEURE_DEBUT, DATEHEURE_FIN, STATUT)
        VALUES
            ('SEA' || LPAD(v_numero_seance, 6, '0'),
             p_film,
             p_salle,
             v_debut,
             v_debut + (p_duree / 1440),
             'OUVERTE');
    END ajouter_seance;

BEGIN
    -- ------------------------------------------------------------------------
    -- SALLE A : LES GARDIENS DU BAOBAB (115 minutes)
    -- Lundi : 14h00 et 20h00
    -- Jeudi : 14h00, 17h00 et 20h00
    -- Dimanche : 15h00
    -- ------------------------------------------------------------------------

    -- Semaine du 16 au 22 novembre
    ajouter_seance('FIL000001', 'SAL000001', '16/11/2026 14:00', 115);
    ajouter_seance('FIL000001', 'SAL000001', '16/11/2026 20:00', 115);

    ajouter_seance('FIL000001', 'SAL000001', '19/11/2026 14:00', 115);
    ajouter_seance('FIL000001', 'SAL000001', '19/11/2026 17:00', 115);
    ajouter_seance('FIL000001', 'SAL000001', '19/11/2026 20:00', 115);

    ajouter_seance('FIL000001', 'SAL000001', '22/11/2026 15:00', 115);

    -- Semaine du 23 au 29 novembre
    ajouter_seance('FIL000001', 'SAL000001', '23/11/2026 14:00', 115);
    ajouter_seance('FIL000001', 'SAL000001', '23/11/2026 20:00', 115);

    ajouter_seance('FIL000001', 'SAL000001', '26/11/2026 14:00', 115);
    ajouter_seance('FIL000001', 'SAL000001', '26/11/2026 17:00', 115);
    ajouter_seance('FIL000001', 'SAL000001', '26/11/2026 20:00', 115);

    ajouter_seance('FIL000001', 'SAL000001', '29/11/2026 15:00', 115);

    -- Dernier lundi de la periode
    ajouter_seance('FIL000001', 'SAL000001', '30/11/2026 14:00', 115);
    ajouter_seance('FIL000001', 'SAL000001', '30/11/2026 20:00', 115);


    -- ------------------------------------------------------------------------
    -- SALLE B : NUIT SUR ANALAMANGA (105 minutes)
    -- Lundi : 14h00, 17h00 et 20h00
    -- Mardi : 18h00
    -- Vendredi : 17h00 et 20h00
    -- ------------------------------------------------------------------------

    -- Semaine du 16 au 22 novembre
    ajouter_seance('FIL000002', 'SAL000002', '16/11/2026 14:00', 105);
    ajouter_seance('FIL000002', 'SAL000002', '16/11/2026 17:00', 105);
    ajouter_seance('FIL000002', 'SAL000002', '16/11/2026 20:00', 105);

    ajouter_seance('FIL000002', 'SAL000002', '17/11/2026 18:00', 105);

    ajouter_seance('FIL000002', 'SAL000002', '20/11/2026 17:00', 105);
    ajouter_seance('FIL000002', 'SAL000002', '20/11/2026 20:00', 105);

    -- Semaine du 23 au 29 novembre
    ajouter_seance('FIL000002', 'SAL000002', '23/11/2026 14:00', 105);
    ajouter_seance('FIL000002', 'SAL000002', '23/11/2026 17:00', 105);
    ajouter_seance('FIL000002', 'SAL000002', '23/11/2026 20:00', 105);

    ajouter_seance('FIL000002', 'SAL000002', '24/11/2026 18:00', 105);

    ajouter_seance('FIL000002', 'SAL000002', '27/11/2026 17:00', 105);
    ajouter_seance('FIL000002', 'SAL000002', '27/11/2026 20:00', 105);

    -- Dernier lundi de la periode
    ajouter_seance('FIL000002', 'SAL000002', '30/11/2026 14:00', 105);
    ajouter_seance('FIL000002', 'SAL000002', '30/11/2026 17:00', 105);
    ajouter_seance('FIL000002', 'SAL000002', '30/11/2026 20:00', 105);


    -- ------------------------------------------------------------------------
    -- SALLE C : LE PETIT LEMURIEN (90 minutes)
    -- Mardi : 14h00
    -- Mercredi : 10h00, 14h00 et 17h00
    -- Samedi : 14h00 et 17h00
    -- ------------------------------------------------------------------------

    -- Semaine du 16 au 22 novembre
    ajouter_seance('FIL000003', 'SAL000003', '17/11/2026 14:00', 90);

    ajouter_seance('FIL000003', 'SAL000003', '18/11/2026 10:00', 90);
    ajouter_seance('FIL000003', 'SAL000003', '18/11/2026 14:00', 90);
    ajouter_seance('FIL000003', 'SAL000003', '18/11/2026 17:00', 90);

    ajouter_seance('FIL000003', 'SAL000003', '21/11/2026 14:00', 90);
    ajouter_seance('FIL000003', 'SAL000003', '21/11/2026 17:00', 90);

    -- Semaine du 23 au 29 novembre
    ajouter_seance('FIL000003', 'SAL000003', '24/11/2026 14:00', 90);

    ajouter_seance('FIL000003', 'SAL000003', '25/11/2026 10:00', 90);
    ajouter_seance('FIL000003', 'SAL000003', '25/11/2026 14:00', 90);
    ajouter_seance('FIL000003', 'SAL000003', '25/11/2026 17:00', 90);

    ajouter_seance('FIL000003', 'SAL000003', '28/11/2026 14:00', 90);
    ajouter_seance('FIL000003', 'SAL000003', '28/11/2026 17:00', 90);
END;
/


-- ============================================================================
-- 9) MENUS CINEMA
-- Seules les pages deja presentes dans le projet sont exposees.
-- ============================================================================

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000001', 'Cinema', 'movie', NULL, 3, 1, NULL);

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000002', 'Reservations', 'event_seat', NULL, 1, 2, 'CINEMA000001');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000003', 'Nouvelle reservation', 'add',
        'module.jsp?but=reservation/reservation-saisie.jsp',
        1, 3, 'CINEMA000002');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000004', 'Categories de places', 'label',
        'module.jsp?but=cinema/categorie-place/categorie-place-liste.jsp',
        2, 2, 'CINEMA000001');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000005', 'Salles', 'theaters', NULL, 3, 2, 'CINEMA000001');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000006', 'Films', 'movie', NULL, 4, 2, 'CINEMA000001');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000007', 'Clients', 'people',
        'module.jsp?but=client/client-liste.jsp',
        6, 2, 'CINEMA000001');

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0001', NULL, 'CINEMA000001', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0002', NULL, 'CINEMA000002', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0003', NULL, 'CINEMA000003', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0004', NULL, 'CINEMA000004', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0005', NULL, 'CINEMA000005', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0006', NULL, 'CINEMA000006', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0007', NULL, 'CINEMA000007', 'dg', NULL, NULL, 0);


-- ============================================================================
-- 10) SOUS-MENUS
-- ============================================================================

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000008', 'Liste des reservations', 'list',
        'module.jsp?but=reservation/reservation-liste.jsp',
        2, 3, 'CINEMA000002');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000009', 'Ventes', 'confirmation_number',
        NULL, 6, 2, 'CINEMA000001');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000010', 'Factures', 'description',
        'module.jsp?but=facturation/facture-client-liste.jsp',
        7, 2, 'CINEMA000001');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000011', 'Nouveau film', 'add',
        'module.jsp?but=cinema/film/film-saisie.jsp',
        2, 3, 'CINEMA000006');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000012', 'Liste des films', 'list',
        'module.jsp?but=cinema/film/film-liste.jsp',
        1, 3, 'CINEMA000006');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000013', 'Liste des salles', 'list',
        'module.jsp?but=cinema/salle/salle-liste.jsp',
        1, 3, 'CINEMA000005');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000014', 'Nouvelle salle', 'add',
        'module.jsp?but=cinema/salle/salle-saisie.jsp',
        2, 3, 'CINEMA000005');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000015', 'Seances', 'event',
        NULL, 5, 2, 'CINEMA000001');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000016', 'Liste des seances', 'list',
        'module.jsp?but=cinema/seance/seance-liste.jsp',
        1, 3, 'CINEMA000015');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000017', 'Nouvelle seance', 'add',
        'module.jsp?but=cinema/seance/seance-saisie.jsp',
        2, 3, 'CINEMA000015');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000019', 'Liste des ventes', 'list',
        'module.jsp?but=vente/vente-liste.jsp',
        1, 3, 'CINEMA000009');

INSERT INTO MENUDYNAMIQUE (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
VALUES ('CINEMA000020', 'Achat direct', 'shopping_cart',
        'module.jsp?but=vente/vente-directe-saisie.jsp',
        2, 3, 'CINEMA000009');


-- ============================================================================
-- 11) DROITS DES MENUS
-- ============================================================================

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0008', NULL, 'CINEMA000008', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0009', NULL, 'CINEMA000009', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0010', NULL, 'CINEMA000010', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0011', NULL, 'CINEMA000011', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0019', NULL, 'CINEMA000019', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0020', NULL, 'CINEMA000020', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0012', NULL, 'CINEMA000012', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0013', NULL, 'CINEMA000013', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0014', NULL, 'CINEMA000014', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0015', NULL, 'CINEMA000015', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0016', NULL, 'CINEMA000016', 'dg', NULL, NULL, 0);

INSERT INTO USERMENU
(ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
VALUES ('CINEMAUSR0017', NULL, 'CINEMA000017', 'dg', NULL, NULL, 0);


-- ============================================================================
-- 12) DROITS APJ EXPLICITES POUR LE PAIEMENT
-- ============================================================================

INSERT INTO ACTIONROLE
(ID, NOMTABLEOBJET, ACTION, ROLEMINIMUM, DIRECTION, UTILISATEUR, GROUPETABLE)
VALUES ('CINEMA-ACT-PAY-001', 'CINE_PAIEMENT', 'insert', 9, NULL, NULL, NULL);

INSERT INTO ACTIONROLE
(ID, NOMTABLEOBJET, ACTION, ROLEMINIMUM, DIRECTION, UTILISATEUR, GROUPETABLE)
VALUES ('CINEMA-ACT-PAY-002', 'FACTURECLIENT', 'insert', 9, NULL, NULL, NULL);

INSERT INTO ACTIONROLE
(ID, NOMTABLEOBJET, ACTION, ROLEMINIMUM, DIRECTION, UTILISATEUR, GROUPETABLE)
VALUES ('CINEMA-ACT-PAY-003', 'FACTURECLIENT_LIGNE', 'insert', 9, NULL, NULL, NULL);

INSERT INTO ACTIONROLE
(ID, NOMTABLEOBJET, ACTION, ROLEMINIMUM, DIRECTION, UTILISATEUR, GROUPETABLE)
VALUES ('CINEMA-ACT-PAY-004', 'LIAISONPAIEMENT', 'insert', 9, NULL, NULL, NULL);

INSERT INTO ACTIONROLE
(ID, NOMTABLEOBJET, ACTION, ROLEMINIMUM, DIRECTION, UTILISATEUR, GROUPETABLE)
VALUES ('CINEMA-ACT-PAY-005', 'MVTCAISSE', 'insert', 9, NULL, NULL, NULL);

INSERT INTO ACTIONROLE
(ID, NOMTABLEOBJET, ACTION, ROLEMINIMUM, DIRECTION, UTILISATEUR, GROUPETABLE)
VALUES ('CINEMA-ACT-PAY-006', 'CINE_BILLET', 'insert', 9, NULL, NULL, NULL);


-- ============================================================================
-- 13) DROITS APJ POUR LA CREATION DU REFERENTIEL CINEMA
-- ============================================================================

INSERT INTO ACTIONROLE
(ID, NOMTABLEOBJET, ACTION, ROLEMINIMUM, DIRECTION, UTILISATEUR, GROUPETABLE)
VALUES ('CINEMA-ACT-CREATE-001', 'CINE_FILM', 'insert', 9, NULL, NULL, NULL);

INSERT INTO ACTIONROLE
(ID, NOMTABLEOBJET, ACTION, ROLEMINIMUM, DIRECTION, UTILISATEUR, GROUPETABLE)
VALUES ('CINEMA-ACT-CREATE-002', 'CINE_SALLE', 'insert', 9, NULL, NULL, NULL);

INSERT INTO ACTIONROLE
(ID, NOMTABLEOBJET, ACTION, ROLEMINIMUM, DIRECTION, UTILISATEUR, GROUPETABLE)
VALUES ('CINEMA-ACT-CREATE-003', 'CINE_PLACE', 'insert', 9, NULL, NULL, NULL);

INSERT INTO ACTIONROLE
(ID, NOMTABLEOBJET, ACTION, ROLEMINIMUM, DIRECTION, UTILISATEUR, GROUPETABLE)
VALUES ('CINEMA-ACT-CREATE-004', 'CINE_SEANCE', 'insert', 9, NULL, NULL, NULL);


-- ============================================================================
-- 14) REDEMARRAGE DES SEQUENCES
-- Les identifiants fixes vont jusqu'a :
-- CLIENT 3, MODEPAIEMENT 3, CAISSE 1, CATEGORIE 3,
-- SALLE 3, PLACE 60, FILM 3, SEANCE 36.
-- Syntaxe ALTER SEQUENCE ... RESTART START WITH : Oracle 18c ou plus recent.
-- ============================================================================

ALTER SEQUENCE SEQ_CLIENT RESTART START WITH 4;
ALTER SEQUENCE SEQ_MODEPAIEMENT RESTART START WITH 4;
ALTER SEQUENCE SEQ_CAISSE RESTART START WITH 2;

ALTER SEQUENCE SEQ_CINE_CATEGORIE_PLACE RESTART START WITH 4;
ALTER SEQUENCE SEQ_REMISE RESTART START WITH 3;
ALTER SEQUENCE SEQ_CINE_SALLE RESTART START WITH 4;
ALTER SEQUENCE SEQ_CINE_PLACE RESTART START WITH 61;
ALTER SEQUENCE SEQ_CINE_FILM RESTART START WITH 4;
ALTER SEQUENCE SEQ_CINE_SEANCE RESTART START WITH 42;


-- ============================================================================
-- 15) VALIDATION
-- ============================================================================

COMMIT;
