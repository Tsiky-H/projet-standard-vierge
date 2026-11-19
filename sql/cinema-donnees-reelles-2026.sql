-- DONNEES REELLES CINEMA - programme du 16/11/2026 au 30/11/2026
-- A executer apres cinema-application-data.sql et apres creation du schema.
-- Script relancable : films, categories, salles, places et seances sont
-- rapproches par leur identifiant stable ou leur cle fonctionnelle.
-- Le schema actuel ne comporte pas encore les colonnes REALISATEUR et GENRE;
-- ces deux informations du PDF ne sont donc pas stockees par ce script.
SET DEFINE OFF
SET SQLBLANKLINES ON
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

-- Categories et tarifs du plan fourni : Standard 15 000 Ar, Confort 20 000 Ar,
-- VIP 30 000 Ar. Les deux premieres categories sont celles du jeu initial.
MERGE INTO CINE_CATEGORIE_PLACE cible
USING (SELECT 'CAT000001' ID, 'Standard' LIBELLE, 15000 PRIX FROM DUAL
       UNION ALL SELECT 'CATREAL03', 'Confort', 20000 FROM DUAL
       UNION ALL SELECT 'CAT000002', 'VIP', 30000 FROM DUAL) source
ON (cible.IDCATEGORIEPLACE = source.ID)
WHEN MATCHED THEN UPDATE SET cible.LIBELLE = source.LIBELLE,
    cible.PRIX_BASE = source.PRIX, cible.ACTIF = 1
WHEN NOT MATCHED THEN INSERT (IDCATEGORIEPLACE, LIBELLE, PRIX_BASE, ACTIF)
    VALUES (source.ID, source.LIBELLE, source.PRIX, 1);

-- Salles A, B et C : chaque rangee contient cinq places.
MERGE INTO CINE_SALLE cible
USING (SELECT 'SALREAL-A' ID, 'Salle A' LIBELLE FROM DUAL
       UNION ALL SELECT 'SALREAL-B', 'Salle B' FROM DUAL
       UNION ALL SELECT 'SALREAL-C', 'Salle C' FROM DUAL) source
ON (UPPER(cible.LIBELLE) = UPPER(source.LIBELLE))
WHEN MATCHED THEN UPDATE SET cible.CAPACITE = 20, cible.ACTIF = 1
WHEN NOT MATCHED THEN INSERT (IDSALLE, LIBELLE, CAPACITE, ACTIF)
    VALUES (source.ID, source.LIBELLE, 20, 1);

-- Cree ou remet en etat les 20 places de chacune des trois salles.
MERGE INTO CINE_PLACE cible
USING (
    WITH numeros AS (SELECT LEVEL n FROM DUAL CONNECT BY LEVEL <= 20),
    salles AS (
        SELECT IDSALLE, SUBSTR(LIBELLE, -1) code_salle
        FROM CINE_SALLE WHERE UPPER(LIBELLE) IN ('SALLE A', 'SALLE B', 'SALLE C')
    )
    SELECT 'PLC-REAL-' || s.code_salle || '-' || TO_CHAR(n.n, 'FM00') IDPLACE,
           s.IDSALLE,
           CHR(65 + FLOOR((n.n - 1) / 5)) || TO_CHAR(MOD(n.n - 1, 5) + 1) NUMERO,
           CHR(65 + FLOOR((n.n - 1) / 5)) RANGEE,
           CASE WHEN n.n <= 10 THEN 'CAT000001'
                WHEN n.n <= 15 THEN 'CATREAL03'
                ELSE 'CAT000002' END IDCATEGORIEPLACE
    FROM salles s CROSS JOIN numeros n
) source
ON (cible.IDSALLE = source.IDSALLE AND cible.NUMERO = source.NUMERO)
WHEN MATCHED THEN UPDATE SET cible.RANGEE = source.RANGEE,
    cible.IDCATEGORIEPLACE = source.IDCATEGORIEPLACE, cible.ACTIF = 1
WHEN NOT MATCHED THEN INSERT
    (IDPLACE, IDSALLE, NUMERO, RANGEE, IDCATEGORIEPLACE, ACTIF)
    VALUES (source.IDPLACE, source.IDSALLE, source.NUMERO,
            source.RANGEE, source.IDCATEGORIEPLACE, 1);

-- Si ces salles existaient deja avec un plan de demonstration, desactive les
-- places hors du plan reel A1-D5 sans supprimer leur historique.
UPDATE CINE_PLACE p
SET ACTIF = 0
WHERE p.IDSALLE IN (SELECT IDSALLE FROM CINE_SALLE
                    WHERE UPPER(LIBELLE) IN ('SALLE A', 'SALLE B', 'SALLE C'))
  AND NOT REGEXP_LIKE(p.NUMERO, '^[A-D][1-5]$');

-- Films du programme. Tous publics est code 0; -12 est code 12.
MERGE INTO CINE_FILM cible
USING (SELECT 'FILREAL001' ID, 'Les Gardiens du Baobab' TITRE, 115 DUREE, 0 AGE FROM DUAL
       UNION ALL SELECT 'FILREAL002', 'Nuit sur Analamanga', 105, 12 FROM DUAL
       UNION ALL SELECT 'FILREAL003', 'Le Petit Lémurien', 90, 0 FROM DUAL) source
ON (UPPER(cible.TITRE) = UPPER(source.TITRE))
WHEN MATCHED THEN UPDATE SET cible.DUREE_MINUTES = source.DUREE,
    cible.CLASSIFICATION_AGE = source.AGE, cible.ACTIF = 1
WHEN NOT MATCHED THEN INSERT
    (IDFILM, TITRE, DUREE_MINUTES, CLASSIFICATION_AGE, ACTIF)
    VALUES (source.ID, source.TITRE, source.DUREE, source.AGE, 1);

-- Programme hebdomadaire repete pour chaque jour compris entre le 16 et le
-- 30 novembre 2026 inclus : lundi=1, ..., dimanche=7 (ISO, independant de la langue).
MERGE INTO CINE_SEANCE cible
USING (
    WITH dates AS (
        SELECT DATE '2026-11-16' + LEVEL - 1 jour
        FROM DUAL CONNECT BY LEVEL <= 15
    ),
    programme AS (
        SELECT 'Les Gardiens du Baobab' TITRE, 1 JOUR_ISO, 14 HEURE, 'Salle A' SALLE FROM DUAL
        UNION ALL SELECT 'Les Gardiens du Baobab', 1, 20, 'Salle A' FROM DUAL
        UNION ALL SELECT 'Les Gardiens du Baobab', 4, 14, 'Salle A' FROM DUAL
        UNION ALL SELECT 'Les Gardiens du Baobab', 4, 17, 'Salle A' FROM DUAL
        UNION ALL SELECT 'Les Gardiens du Baobab', 4, 20, 'Salle A' FROM DUAL
        UNION ALL SELECT 'Les Gardiens du Baobab', 7, 15, 'Salle A' FROM DUAL
        UNION ALL SELECT 'Nuit sur Analamanga', 1, 14, 'Salle B' FROM DUAL
        UNION ALL SELECT 'Nuit sur Analamanga', 1, 17, 'Salle B' FROM DUAL
        UNION ALL SELECT 'Nuit sur Analamanga', 1, 20, 'Salle B' FROM DUAL
        UNION ALL SELECT 'Nuit sur Analamanga', 2, 18, 'Salle B' FROM DUAL
        UNION ALL SELECT 'Nuit sur Analamanga', 5, 17, 'Salle B' FROM DUAL
        UNION ALL SELECT 'Nuit sur Analamanga', 5, 20, 'Salle B' FROM DUAL
        UNION ALL SELECT 'Le Petit Lémurien', 2, 14, 'Salle C' FROM DUAL
        UNION ALL SELECT 'Le Petit Lémurien', 3, 14, 'Salle C' FROM DUAL
        UNION ALL SELECT 'Le Petit Lémurien', 3, 17, 'Salle C' FROM DUAL
        UNION ALL SELECT 'Le Petit Lémurien', 6, 10, 'Salle C' FROM DUAL
        UNION ALL SELECT 'Le Petit Lémurien', 6, 14, 'Salle C' FROM DUAL
        UNION ALL SELECT 'Le Petit Lémurien', 6, 17, 'Salle C' FROM DUAL
    ),
    creneaux AS (
        SELECT f.IDFILM, s.IDSALLE,
               d.jour + p.HEURE / 24 DATEHEURE_DEBUT,
               d.jour + p.HEURE / 24 + f.DUREE_MINUTES / 1440 DATEHEURE_FIN
        FROM dates d
        JOIN programme p ON TRUNC(d.jour) - TRUNC(d.jour, 'IW') + 1 = p.JOUR_ISO
        JOIN CINE_FILM f ON UPPER(f.TITRE) = UPPER(p.TITRE) AND f.ACTIF = 1
        JOIN CINE_SALLE s ON UPPER(s.LIBELLE) = UPPER(p.SALLE) AND s.ACTIF = 1
    )
    SELECT 'SREAL' || TO_CHAR(ROW_NUMBER() OVER
               (ORDER BY DATEHEURE_DEBUT, IDFILM, IDSALLE), 'FM0000') IDSEANCE,
           IDFILM, IDSALLE, DATEHEURE_DEBUT, DATEHEURE_FIN,
           CASE WHEN DATEHEURE_FIN <= SYSDATE THEN 'TERMINEE'
                WHEN DATEHEURE_DEBUT <= SYSDATE THEN 'OUVERTE'
                ELSE 'PLANIFIEE' END STATUT
    FROM creneaux
) source
ON (cible.IDFILM = source.IDFILM AND cible.IDSALLE = source.IDSALLE
    AND cible.DATEHEURE_DEBUT = source.DATEHEURE_DEBUT)
WHEN MATCHED THEN UPDATE SET cible.DATEHEURE_FIN = source.DATEHEURE_FIN,
    cible.STATUT = source.STATUT
WHEN NOT MATCHED THEN INSERT
    (IDSEANCE, IDFILM, IDSALLE, DATEHEURE_DEBUT, DATEHEURE_FIN, STATUT)
    VALUES (source.IDSEANCE, source.IDFILM, source.IDSALLE,
            source.DATEHEURE_DEBUT, source.DATEHEURE_FIN, source.STATUT);

COMMIT;
PROMPT Donnees reelles du programme cinema 16-30 novembre 2026 inserees.
