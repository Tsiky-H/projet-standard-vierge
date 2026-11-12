# Inventaire des vues SQL — Cinéma APJ

Ce document fixe le premier lot de vues de lecture et leurs contrats de colonnes pour les classes APJ `Lib`. Le principe retenu est : écrire sur les tables métier, lire sur des vues enrichies. Les vues ne servent pas aux créations ni aux modifications.

Les noms de classes `Lib` et les alias proposés sont des conventions. Les propriétés Java doivent correspondre aux alias SQL (la casse n’a pas d’importance), avec des types compatibles JDBC. Les clés restent celles des tables source.

## Premier lot — référentiels et séances

### `CINE_CATEGORIE_PLACE_LIB`

- **Classe créée** : `cinema.CategoriePlaceLib extends cinema.CategoriePlace`.
- **Utilisation** : liste et fiche catégorie; sélecteur dans la saisie d’une place.
- **Grain** : une ligne par catégorie.
- **Colonnes** : `IDCATEGORIEPLACE VARCHAR2(20)` (clé), `CODE VARCHAR2(20)`, `LIBELLE VARCHAR2(80)`, `PRIX_BASE NUMBER(12,2)`, `ACTIF NUMBER(1)`.
- Cette vue n’a pas besoin de jointure. Le suffixe `Lib` est une convention de lecture, pas une obligation d’avoir un champ calculé.

### `CINE_SALLE_LIB`

- **Classe créée** : `cinema.SalleLib extends cinema.Salle`.
- **Utilisation** : liste des salles (`PageRecherche`) et fiche salle (`PageConsulte`).
- **Grain** : une ligne par salle.
- **Colonnes** : `IDSALLE VARCHAR2(20)` (clé), `CODE VARCHAR2(20)`, `LIBELLE VARCHAR2(100)`, `CAPACITE NUMBER(6)`, `ACTIF NUMBER(1)`, `NOMBRE_PLACES NUMBER`.
- `NOMBRE_PLACES` compte les places configurées dans `CINE_PLACE`, actives ou non. Calculer l’agrégat dans une sous-requête groupée pour éviter de dupliquer les salles; afficher 0 lorsqu’une salle n’a pas encore de place.

### `CINE_PLACE_LIB`

- **Classe créée** : `cinema.PlaceLib extends cinema.Place`.
- **Utilisation** : tableau détaillé du plan, fiche place et données du rendu graphique.
- **Grain** : une ligne par place.
- **Colonnes** : `IDPLACE VARCHAR2(20)` (clé), `IDSALLE VARCHAR2(20)`, `IDSALLELIB VARCHAR2(100)`, `NUMERO VARCHAR2(20)`, `RANGEE VARCHAR2(20)`, `IDCATEGORIEPLACE VARCHAR2(20)`, `IDCATEGORIEPLACELIB VARCHAR2(80)`, `PRIX_BASE NUMBER(12,2)`, `ACTIF NUMBER(1)`.
- Les identifiants salle et catégorie restent exposés pour les filtres et liens; leurs colonnes `...LIB` portent les libellés joints. Cette convention suit les exemples du projet de référence (`IDMACHINE` / `IDMACHINELIB`).

### `CINE_FILM_LIB`

- **Classe créée** : `cinema.FilmLib extends cinema.Film`.
- **Utilisation** : liste et fiche film; contexte de lecture des séances.
- **Grain** : une ligne par film.
- **Colonnes** : `IDFILM VARCHAR2(20)` (clé), `CODE VARCHAR2(20)`, `TITRE VARCHAR2(200)`, `DUREE_MINUTES NUMBER(4)`, `CLASSIFICATION_AGE NUMBER(3)` (nullable), `ACTIF NUMBER(1)`.

### `CINE_SEANCE_LIB`

- **Classe créée** : `cinema.SeanceLib extends cinema.Seance`.
- **Utilisation** : liste et fiche séance; choix de séance dans réservation et vente.
- **Grain** : une ligne par séance.
- **Colonnes** : `IDSEANCE VARCHAR2(20)` (clé), `IDFILM VARCHAR2(20)`, `IDFILMLIB VARCHAR2(200)`, `IDSALLE VARCHAR2(20)`, `IDSALLELIB VARCHAR2(100)`, `DATEHEURE_DEBUT DATE`, `DATEHEURE_FIN DATE`, `STATUT VARCHAR2(20)`.
- Les identifiants d’origine restent disponibles pour les critères; les colonnes `IDFILMLIB` et `IDSALLELIB` portent les libellés joints selon la convention APJ observée.

## Disponibilité des places

### `CINE_PLACE_SEANCE_DISPO`

- **Classe créée** : `cinema.PlaceSeanceDispo extends cinema.PlaceLib`, dédiée à la lecture filtrée par séance.
- **Utilisation** : plan interactif lors d’une réservation ou vente.
- **Grain** : une ligne par couple séance/place.
- **Colonnes** : `IDSEANCE`, `IDPLACE`, `IDSALLE`, `IDSALLELIB`, `IDFILM`, `IDFILMLIB`, `DATEHEURE_DEBUT`, `DATEHEURE_FIN`, `NUMERO`, `RANGEE`, `IDCATEGORIEPLACE`, `IDCATEGORIEPLACELIB`, `PRIX_BASE`, `ACTIF`, `SOURCE_BLOCAGE`, `STATUT_OCCUPATION`, `DISPONIBLE`.
- Une place active d’une séance `PLANIFIEE` ou `OUVERTE` est disponible si aucune ligne bloquante de réservation ou de vente ne la concerne. Une ligne de réservation `BLOQUEE` ou `ACHETEE` rend la place indisponible; si l’échéance passe sans paiement valide, le service expire la réservation et ses places dans la même transaction, ce qui la libère. Un paiement valide reçu à temps garde la réservation bloquante pendant l’attente de validation de la facture. Une ligne de vente `BLOQUEE` ou `ACHETEE` bloque également la place. Les lignes `EXPIREE` ou `ANNULEE` ne bloquent pas.
- `SOURCE_BLOCAGE` vaut `RESERVATION`, `VENTE` ou `NULL`. `STATUT_OCCUPATION` expose `BLOQUEE`, `ACHETEE`, `DISPONIBLE` ou `INDISPONIBLE`; `DISPONIBLE` vaut 1 ou 0.
- La vue ne garantit pas la réservation : le service doit revérifier et verrouiller en transaction au moment de l’écriture.

## Vues suivantes — entêtes et détails distincts

| Vue | Grain et utilisation |
|---|---|
| `CINE_RESERVATION_LIB` | Une ligne par réservation; liste/fiche avec client, mode prévu, dates, statut, quantité totale et montant. |
| `CINE_RESERVATION_DETAIL_LIB` | Une ligne par détail de réservation/séance; film, salle, horaires, quantité et montant de ligne. |
| `CINE_RESERVATION_PLACE_LIB` | Une ligne par place réservée; place, catégorie, prix figé, statut et expiration. |
| `CINE_VENTE_LIB` | Une ligne par vente; type, client, caisse, réservation et facture liées, montant et statut. |
| `CINE_VENTEDETAIL_LIB` | Une ligne par détail; séance, film, salle, place, catégorie, prix figé et statut. |
| `CINE_FACTURECLIENT_LIB` | Une ligne par facture ou avoir; client, document d’origine, statut métier `STATUT`, état APJ `ETAT` et montants. |
| `CINE_FACTURECLIENT_LIGNE_LIB` | Une ligne par ligne de facture/avoir; désignation, quantité, prix, TVA, montant et source/origine. |
| `CINE_PAIEMENT_LIB` | Une ligne par paiement; mode, statut, montant, référence et réservation ou vente source. |
| `CINE_LIAISONPAIEMENT_LIB` | Une ligne par affectation paiement/facture avec montant affecté. |
| `CINE_MVTCAISSE_LIB` | Une ligne par mouvement; caisse, paiement, facture, origine, type, statut métier et `ETAT` APJ. |
| `CINE_BILLET_LIB` | Une ligne par billet; numéro, statut, séance, film, salle, place et source vente/réservation. |

Les vues d’entête et de détail restent séparées pour qu’une jointure de
plusieurs collections de lignes ne multiplie pas les entêtes ni leurs totaux.

## Synthèses facultatives

| Vue | Utilisation |
|---|---|
| `CINE_CAISSE_SOLDE_LIB` | Une ligne par caisse; comparer le solde théorique aux mouvements métier actifs. |
| `CINE_SEANCE_OCCUPATION_LIB` | Une ligne par séance; capacité, places bloquées, achetées et disponibles pour une synthèse. |

## Workflow APJ et `ETAT`

La discussion PDF décrit `ClassEtat` ainsi : `ETAT = 0` annulé, `ETAT = 1`
créé, `ETAT = 11` validé et `ETAT = 9` clôturé. Une validation simple fait
passer un objet d’état 1 à 10 à l’état 11, sauf workflow hiérarchique. Les vues
de facture et de mouvement exposeront séparément le `STATUT` métier et l’`ETAT`
APJ numérique. On ajoutera un libellé seulement si le socle fournit une table
ou une convention de libellés confirmée.

## Ordre proposé

1. `CINE_CATEGORIE_PLACE_LIB` — vue SQL et classe `CategoriePlaceLib` créées.
2. `CINE_SALLE_LIB` — vue SQL et classe `SalleLib` créées.
3. `CINE_PLACE_LIB` — vue SQL et classe `PlaceLib` créées.
4. `CINE_FILM_LIB` — vue SQL et classe `FilmLib` créées.
5. `CINE_SEANCE_LIB` — vue SQL et classe `SeanceLib` créées.
6. `CINE_PLACE_SEANCE_DISPO` — vue SQL et classe de lecture `PlaceSeanceDispo` créées.

Pour chaque vue, confirmer le nom, le grain, les alias, les types Oracle/JDBC
et la classe APJ avant d’ajouter son `CREATE OR REPLACE VIEW` au script SQL.
Les vues du premier lot sont de lecture; les formulaires d’écriture ciblent
les tables.

## Références

- [Règles métier](./Cinema.md)
- [Parcours des écrans](./Ecrans-Cinema.md)
- [Schéma métier](./sql/cinema-application-final.sql)
- [Schéma APJ](./sql/framework-base.sql)
- Guide APJ du projet de référence : `reference/projet-standard---Copie/docs/APJ-guide.md`
