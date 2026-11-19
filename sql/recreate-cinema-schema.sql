-- RECONSTRUCTION COMPLETE DU SCHEMA CINEMA (DEVELOPPEMENT UNIQUEMENT)
-- ATTENTION : supprime toutes les tables et donnees APJ/Cinema du schema CINEMA,
-- puis recree le framework, le schema metier et les donnees de demonstration.
-- Ne pas lancer si des donnees doivent etre conservees.
-- Tous les scripts enfants doivent se trouver dans le meme repertoire.

SET DEFINE OFF
SET SQLBLANKLINES ON
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

@@cleanup-cinema-schema.sql
@@framework-base.sql
@@cinema-application-final.sql


PROMPT Reconstruction complete du schema CINEMA terminee.
