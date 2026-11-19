-- Mise a jour idempotente du menu Cinema sans toucher aux donnees metier.
-- A executer apres le schema et les donnees initiales.
SET DEFINE OFF
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

MERGE INTO MENUDYNAMIQUE cible
USING (SELECT 'CINEMA000005' ID, 'Salles' LIBELLE, 'theaters' ICONE,
              CAST(NULL AS VARCHAR2(250)) HREF, 3 RANG, 2 NIVEAU,
              'CINEMA000001' ID_PERE FROM DUAL) source
ON (cible.ID = source.ID)
WHEN MATCHED THEN UPDATE SET cible.LIBELLE=source.LIBELLE, cible.ICONE=source.ICONE,
    cible.HREF=source.HREF, cible.RANG=source.RANG, cible.NIVEAU=source.NIVEAU,
    cible.ID_PERE=source.ID_PERE;

MERGE INTO MENUDYNAMIQUE cible
USING (SELECT 'CINEMA000009' ID, 'Ventes' LIBELLE, 'confirmation_number' ICONE,
              CAST(NULL AS VARCHAR2(250)) HREF, 6 RANG, 2 NIVEAU,
              'CINEMA000001' ID_PERE FROM DUAL) source
ON (cible.ID = source.ID)
WHEN MATCHED THEN UPDATE SET cible.LIBELLE=source.LIBELLE, cible.ICONE=source.ICONE,
    cible.HREF=source.HREF, cible.RANG=source.RANG, cible.NIVEAU=source.NIVEAU,
    cible.ID_PERE=source.ID_PERE;

MERGE INTO MENUDYNAMIQUE cible
USING (SELECT 'CINEMA000006' ID, 'Films' LIBELLE, 'movie' ICONE,
              CAST(NULL AS VARCHAR2(250)) HREF, 4 RANG, 2 NIVEAU,
              'CINEMA000001' ID_PERE FROM DUAL) source
ON (cible.ID = source.ID)
WHEN MATCHED THEN UPDATE SET cible.LIBELLE=source.LIBELLE, cible.ICONE=source.ICONE,
    cible.HREF=source.HREF, cible.RANG=source.RANG, cible.NIVEAU=source.NIVEAU,
    cible.ID_PERE=source.ID_PERE;

MERGE INTO MENUDYNAMIQUE cible
USING (SELECT 'CINEMA000011' ID, 'Nouveau film' LIBELLE, 'add' ICONE,
              'module.jsp?but=cinema/film/film-saisie.jsp' HREF,
              2 RANG, 3 NIVEAU, 'CINEMA000006' ID_PERE FROM DUAL) source
ON (cible.ID = source.ID)
WHEN MATCHED THEN UPDATE SET cible.LIBELLE=source.LIBELLE, cible.ICONE=source.ICONE,
    cible.HREF=source.HREF, cible.RANG=source.RANG, cible.NIVEAU=source.NIVEAU,
    cible.ID_PERE=source.ID_PERE;

MERGE INTO MENUDYNAMIQUE cible
USING (
    SELECT 'CINEMA000012' ID, 'Liste des films' LIBELLE, 'list' ICONE,
           'module.jsp?but=cinema/film/film-liste.jsp' HREF,
           1 RANG, 3 NIVEAU, 'CINEMA000006' ID_PERE FROM DUAL
    UNION ALL SELECT 'CINEMA000013', 'Liste des salles', 'list',
           'module.jsp?but=cinema/salle/salle-liste.jsp', 1, 3, 'CINEMA000005' FROM DUAL
    UNION ALL SELECT 'CINEMA000014', 'Nouvelle salle', 'add',
           'module.jsp?but=cinema/salle/salle-saisie.jsp', 2, 3, 'CINEMA000005' FROM DUAL
    UNION ALL SELECT 'CINEMA000015', 'Séances', 'event', NULL,
           5, 2, 'CINEMA000001' FROM DUAL
    UNION ALL SELECT 'CINEMA000016', 'Liste des séances', 'list',
           'module.jsp?but=cinema/seance/seance-liste.jsp', 1, 3, 'CINEMA000015' FROM DUAL
    UNION ALL SELECT 'CINEMA000017', 'Nouvelle séance', 'add',
           'module.jsp?but=cinema/seance/seance-saisie.jsp', 2, 3, 'CINEMA000015' FROM DUAL
    UNION ALL SELECT 'CINEMA000019', 'Liste des ventes', 'list',
           'module.jsp?but=vente/vente-liste.jsp', 1, 3, 'CINEMA000009' FROM DUAL
    UNION ALL SELECT 'CINEMA000020', 'Achat direct', 'shopping_cart',
           'module.jsp?but=vente/vente-directe-saisie.jsp', 2, 3, 'CINEMA000009' FROM DUAL
         UNION ALL SELECT 'CINEMA000021', 'Remises', 'percent', NULL,
           7, 2, 'CINEMA000001' FROM DUAL
         UNION ALL SELECT 'CINEMA000022', 'Liste des remises', 'list',
           'module.jsp?but=cinema/remise/remise-liste.jsp', 1, 3, 'CINEMA000021' FROM DUAL
         UNION ALL SELECT 'CINEMA000023', 'Nouvelle remise', 'add',
           'module.jsp?but=cinema/remise/remise-saisie.jsp', 2, 3, 'CINEMA000021' FROM DUAL
) source
ON (cible.ID = source.ID)
WHEN MATCHED THEN UPDATE SET cible.LIBELLE=source.LIBELLE, cible.ICONE=source.ICONE,
    cible.HREF=source.HREF, cible.RANG=source.RANG, cible.NIVEAU=source.NIVEAU,
    cible.ID_PERE=source.ID_PERE
WHEN NOT MATCHED THEN INSERT (ID, LIBELLE, ICONE, HREF, RANG, NIVEAU, ID_PERE)
    VALUES (source.ID, source.LIBELLE, source.ICONE, source.HREF,
            source.RANG, source.NIVEAU, source.ID_PERE);

MERGE INTO USERMENU cible
USING (
    SELECT 'CINEMAUSR0012' ID, 'CINEMA000012' IDMENU FROM DUAL
    UNION ALL SELECT 'CINEMAUSR0013', 'CINEMA000013' FROM DUAL
    UNION ALL SELECT 'CINEMAUSR0014', 'CINEMA000014' FROM DUAL
    UNION ALL SELECT 'CINEMAUSR0015', 'CINEMA000015' FROM DUAL
    UNION ALL SELECT 'CINEMAUSR0016', 'CINEMA000016' FROM DUAL
    UNION ALL SELECT 'CINEMAUSR0017', 'CINEMA000017' FROM DUAL
    UNION ALL SELECT 'CINEMAUSR0019', 'CINEMA000019' FROM DUAL
    UNION ALL SELECT 'CINEMAUSR0020', 'CINEMA000020' FROM DUAL
    UNION ALL SELECT 'CINEMAUSR0021', 'CINEMA000021' FROM DUAL
    UNION ALL SELECT 'CINEMAUSR0022', 'CINEMA000022' FROM DUAL
    UNION ALL SELECT 'CINEMAUSR0023', 'CINEMA000023' FROM DUAL
) source
ON (cible.ID = source.ID)
WHEN MATCHED THEN UPDATE SET cible.IDMENU=source.IDMENU, cible.REFUSER=NULL,
    cible.IDROLE='dg', cible.CODESERVICE=NULL, cible.CODEDIR=NULL, cible.INTERDIT=0
WHEN NOT MATCHED THEN INSERT (ID, REFUSER, IDMENU, IDROLE, CODESERVICE, CODEDIR, INTERDIT)
    VALUES (source.ID, NULL, source.IDMENU, 'dg', NULL, NULL, 0);

DELETE FROM USERMENU WHERE ID = 'CINEMAUSR0018' OR IDMENU = 'CINEMA000018';
DELETE FROM MENUDYNAMIQUE WHERE ID = 'CINEMA000018';

COMMIT;
