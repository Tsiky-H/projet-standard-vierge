# Spécification métier du cinéma (APJ)

Ce document fixe les règles métier retenues pour la première version. Il décrit le comportement attendu; il ne signifie pas que les classes, écrans ou scripts SQL correspondants sont déjà implémentés.

## 1. Règles de gestion

### 1.1 Salles, places et catégories

- Un cinéma possède plusieurs salles; chaque salle contient des places numérotées.
- Chaque place appartient à une catégorie. Son prix de base dépend de cette catégorie.
- Le prix appliqué est copié dans la réservation ou la vente au moment de l'opération afin de conserver l'historique du prix.

### 1.2 Films et séances

- Un film peut avoir plusieurs séances; une séance se déroule dans une seule salle.
- Une salle ne peut pas accueillir deux séances qui se chevauchent. Le contrôle est transactionnel.

### 1.3 Réservations

- Une réservation mère peut regrouper plusieurs séances. Chaque détail correspond à une séance et contient les places réservées pour celle-ci.
- À la création, le système crée dans la même transaction la réservation, ses détails et places bloquées, ainsi qu'une ligne `VENTE` de type `RESERVATION`, liée à cette réservation et au statut `BROUILLON`.
- La caisse n'est pas encore connue à la création de cette vente brouillon. `VENTE.IDCAISSE` reste nul jusqu'à l'enregistrement du paiement; le service de paiement y affecte alors la caisse active utilisée pour encaisser.
- La ligne `VENTE` existe avant toute tentative de paiement. Chaque paiement, valide ou refusé, référence cette vente par `CINE_PAIEMENT.IDVENTE`; le paiement ne référence pas directement la réservation. `VENTE.IDRESERVATION` conserve le lien vers la réservation source.
- Un paiement refusé ou non abouti ne confirme pas la réservation et ne valide pas la vente. Le client peut réessayer jusqu'à l'expiration; les places restent bloquées jusque-là et la vente reste `BROUILLON`.
- Un paiement reçu après l'expiration est refusé et ne confirme pas la réservation.
- Si aucun paiement valide n'est reçu à temps, la réservation passe à `EXPIREE`, la vente liée passe à `ANNULEE` et les places sont libérées. Le contrôle du paiement à temps retrouve la réservation en passant par `CINE_PAIEMENT.IDVENTE` puis `VENTE.IDRESERVATION`.
- Si le paiement est reçu avant l'expiration, la réservation ne doit pas expirer pendant qu'elle attend une décision interne. La vente reste `BROUILLON` et les places restent bloquées jusqu'à la décision sur la facture, même si la date d'expiration initiale est passée.
- Une facture refusée mais corrigeable peut être corrigée puis soumise à nouveau. La date d'expiration initiale ne change pas; elle ne réactive pas l'expiration d'une réservation déjà payée.
- Après validation APJ de la facture, la réservation passe à `CONFIRMEE`, la vente à `VALIDEE`, les places à `ACHETEE` et les billets sont émis.
- Si la facture est définitivement refusée et ne peut pas être corrigée, la réservation et la vente sont annulées immédiatement, les places sont libérées et le paiement reçu est remboursé.
- Une réservation non payée peut être annulée à la demande du client avant son expiration; la réservation et sa vente brouillon sont annulées et les places libérées immédiatement.

### 1.4 Vente directe

- Une vente directe permet d'acheter des billets sans réservation préalable; les contraintes d'unicité d'une place par séance s'appliquent aussi.
- La ligne `VENTE` brouillon est créée avant l'enregistrement des tentatives de paiement; les paiements valides ou refusés la référencent par `CINE_PAIEMENT.IDVENTE`.
- Après paiement valide, les places sont bloquées pendant la validation de la facture. Elles ne sont acquises définitivement qu'après validation APJ de la facture.
- Si la facture est corrigeable, la vente et le blocage des places restent en attente pendant sa correction et sa nouvelle soumission.
- Si la facture est définitivement refusée, la vente est annulée, les places sont libérées et le paiement est remboursé. Aucun billet n'est émis; tout billet déjà créé est invalidé.
- Les règles de demande et d'approbation de remboursement décrites ci-dessous s'appliquent aussi aux ventes directes.

### 1.5 Paiement, facture et caisse

- Le mode de paiement est choisi à la réservation comme mode prévu, puis confirmé ou ajusté lors du paiement réel.
- Tout paiement cinéma référence une ligne `VENTE` par `IDVENTE`. Pour une réservation, la vente de type `RESERVATION` porte le lien à la réservation dans `VENTE.IDRESERVATION`.
- Un paiement valide est lié à une facture client. Il déclenche la création de la facture payée et du mouvement d'encaissement.
- La facture et le mouvement de caisse ont deux états distincts: `STATUT` conserve l'état métier; `ETAT` porte le workflow de validation APJ.
- La validation APJ est manuelle pour les factures et les mouvements de caisse. Les valeurs numériques et transitions d'`ETAT` doivent être alignées sur le fonctionnement réel du framework avant l'implémentation.
- La réservation ou la vente devient définitive lorsque le paiement est valide et que la facture est payée et validée par APJ (`ETAT`). Le mouvement de caisse ne bloque pas la confirmation ni l'émission des billets.
- Un mouvement d'encaissement est créé dès que le paiement est valide. Sa validation APJ est un contrôle de caisse séparé.
- Si un mouvement de caisse est refusé, le superutilisateur le corrige et le soumet à nouveau si possible. Sinon, il l'annule sans le supprimer et crée un mouvement correct lié au mouvement remplacé. Cela n'annule pas la vente ni le paiement du client.
- Une correction de mouvement ne constitue pas un remboursement. Un décaissement de remboursement n'est enregistré que pour l'argent effectivement rendu au client.
- Pour la première version, un seul superutilisateur a accès à toutes les données. Aucun `IDUSER` n'est ajouté pour restreindre la visibilité par créateur.

### 1.6 Annulations et remboursements

- Le client peut demander le remboursement d'une réservation payée ou d'une vente directe, en totalité ou pour certains billets seulement.
- La demande doit être reçue avant le début de la séance correspondant à chaque billet. Pour une réservation de plusieurs séances, l'échéance est vérifiée billet par billet.
- Une demande reçue avant le début reste recevable même si le superutilisateur ne la traite qu'après le début. Une demande envoyée après le début est refusée.
- Le remboursement nécessite l'approbation du superutilisateur.
- Si la demande est approuvée, les billets concernés sont annulés et les places libérées. Le système crée un avoir de type document, lié à la facture d'origine, ainsi qu'un mouvement de caisse de type `DECAISSEMENT`. Cette création est déclenchée par l'approbation.
- Si le remboursement est refusé, la vente reste valable. Pour une réservation ou une vente directe dont la facture n'était pas encore validée, la facture de paiement est validée et l'opération est finalisée; une vente déjà finalisée reste inchangée.
- Un avoir conserve la facture d'origine: une facture déjà validée n'est ni modifiée ni supprimée. Les montants de l'avoir ne peuvent pas dépasser les montants restant remboursables de la facture d'origine.
- Pour la première version, l'avoir est représenté comme un document de type distinct dans `FACTURECLIENT` et référence sa facture d'origine; une table `AVOIR` séparée n'est pas prévue.

### 1.7 Statuts et traçabilité APJ

- `STATUT` représente les états métier du cinéma (par exemple `EN_ATTENTE`, `CONFIRMEE`, `EXPIREE`, `PAYEE`, `ANNULEE`). `ETAT` est réservé au workflow APJ sur les factures et les mouvements de caisse.
- Les référentiels, réservations, ventes, paiements et lignes n'utilisent pas `ETAT` par défaut; leurs règles métier sont gérées par les services et leurs classes APJ adaptées.
- L'historique APJ des actions reste distinct du workflow `ETAT`.
- Les opérations de blocage/libération des places, de paiement, de remboursement, de confirmation et d'émission des billets doivent être cohérentes et transactionnelles.
- Une place ne peut être active que dans une seule réservation ou vente pour une même séance.

## 2. Classes Java prévues

Le schéma contient 19 tables métier; chacune aura un modèle Java APJ. L'inventaire
ci-dessous précise la classe de base APJ. Les classes de réservation et de vente
restent des `ClassMAPTable`: elles n'ont pas de colonne `ETAT`, et leurs opérations
composées seront gérées par des services transactionnels. Dans APJ, `ClassMere` et
`ClassFille` héritent de `ClassEtat`; on les réserve donc au document de facture,
qui utilise effectivement le workflow APJ.

### 2.1 Classes déjà présentes

- `cinema.Salle` — table `CINE_SALLE`, hérite de `ClassMAPTable`.
- `client.Client` — table `CLIENT`, hérite de `ClassMAPTable`.
- `caisse.Caisse` — table `CAISSE`, hérite de `ClassMAPTable`.
- `caisse.MvtCaisse` — table `MVTCAISSE`, hérite de `ClassEtat`.
- `paiement.ModePaiement` — table `MODEPAIEMENT`, hérite de `ClassMAPTable`.

### 2.2 Modèles à créer — référentiels (4)

| Classe Java | Table | Héritage APJ |
|---|---|---|
| `cinema.CategoriePlace` | `CINE_CATEGORIE_PLACE` | `ClassMAPTable` |
| `cinema.Place` | `CINE_PLACE` | `ClassMAPTable` |
| `cinema.Film` | `CINE_FILM` | `ClassMAPTable` |
| `cinema.Seance` | `CINE_SEANCE` | `ClassMAPTable` |

### 2.3 Modèles à créer — réservation et vente (6)

| Classe Java | Table | Héritage APJ |
|---|---|---|
| `reservation.Reservation` | `CINE_RESERVATION` | `ClassMAPTable` |
| `reservation.ReservationDetail` | `CINE_RESERVATION_DETAIL` | `ClassMAPTable` |
| `reservation.ReservationPlace` | `CINE_RESERVATION_PLACE` | `ClassMAPTable` |
| `vente.Vente` | `VENTE` | `ClassMAPTable` |
| `vente.VenteDetail` | `VENTEDETAILS` | `ClassMAPTable` |
| `vente.Billet` | `CINE_BILLET` | `ClassMAPTable` |

### 2.4 Modèles à créer — facturation et paiement (4)

| Classe Java | Table | Héritage APJ |
|---|---|---|
| `facturation.FactureClient` | `FACTURECLIENT` | `ClassMere` (qui hérite de `ClassEtat`) |
| `facturation.FactureClientLigne` | `FACTURECLIENT_LIGNE` | `ClassFille` (qui hérite de `ClassEtat`) |
| `paiement.Paiement` | `CINE_PAIEMENT` | `ClassMAPTable` |
| `paiement.LiaisonPaiement` | `LIAISONPAIEMENT` | `ClassMAPTable` |

`FactureClient` regroupe les lignes de facture et d'avoir; un avoir référence la
facture d'origine et ses lignes d'origine. `FACTURECLIENT_LIGNE` porte `ETAT`
pour satisfaire le contrat APJ de `ClassFille`, qui hérite de `ClassEtat`.
Le décompte des 19 modèles est :
8 référentiels (dont quatre déjà présents), 6 modèles de réservation/vente et
5 modèles de facturation, paiement et caisse (dont `MvtCaisse` déjà présente).

### 2.5 Classes d'infrastructure APJ déjà intégrées

Ces classes viennent du projet standard et ont été adaptées au schéma cinéma.
Elles ne font pas partie des 19 modèles métier et n'héritent pas de
`ClassMAPTable` :

- `user.UserEJB` — interface du service APJ.
- `user.UserEJBBean` — implémentation du service et session utilisateur, sans
  dépendances métier Arbiochem/CNAPS inutilisées.
- `user.CreateObject`, `user.UpdateObject`, `user.ValiderObject` — helpers
  génériques appelés par le service APJ.

### 2.6 Points d'extension APJ pour les contrôles métier

- À la création, `UserEJB.createObject(o, c)` délègue à `o.createObject(..., c)`;
  l'implémentation APJ de `ClassMAPTable` appelle `controler(c)` avant
  l'insertion. Le contrôle et l'écriture utilisent la même connexion.
- À la modification, `UserEJB.updateObject(o, c)` n'appelle pas automatiquement
  le contrôle métier. Le chemin `UserEJB.updateObjectValidation(o, c)` appelle
  `controlerUpdate(c)` avant l'écriture, sur la même connexion. Sa surcharge
  sans connexion ouvre la transaction et la valide après l'opération.
- Les futures pages de modification cinéma doivent donc utiliser
  `updateObjectValidation` pour les objets qui nécessitent un contrôle métier
  avant mise à jour. Il n'existe pas encore de JSP propre au cinéma; ce choix
  devra être appliqué lors de leur création.
- Pour les opérations composées d'un service, la surcharge avec `Connection`
  réutilise la connexion fournie et laisse son propriétaire gérer commit,
  rollback et fermeture. La surcharge sans connexion ouvre une connexion,
  désactive l'autocommit, appelle la surcharge avec connexion, puis commit ou
  rollback et ferme la connexion.

### 2.7 Services métier à créer après les modèles

Ces classes portent les opérations multi-tables et les contrôles transactionnels;
elles ne sont pas des modèles APJ et n'héritent pas de `ClassMAPTable`.

- `service.ReservationService` — création de la réservation et de sa vente
  brouillon liée, disponibilité, blocage/libération, expiration, confirmation
  et annulation.
- `service.VenteService` — création des ventes directes brouillon et blocage
  transactionnel des places.
- `service.PaiementService` — paiement, remboursement, billet et mouvements liés.
- `service.FacturationService` — facture, avoir et transitions de validation APJ.

Les classes de lecture `Lib`/`Cpl`, servlets et contrôleurs seront ajoutés quand
les écrans en auront besoin; ils ne sont pas nécessaires pour commencer les
modèles métier.

## 3. Flux métier

### Flux A — Réservation puis paiement

1. Le client choisit film, séance et places; le système contrôle leur disponibilité dans une transaction.
2. Le système crée la réservation, ses détails et places bloquées, ainsi qu'une vente `BROUILLON` de type `RESERVATION` liée à celle-ci, avec date d'expiration et prix appliqués.
3. Chaque tentative de paiement référence cette vente. En cas d'échec ou de refus, la réservation reste en attente, la vente reste brouillon et le client peut réessayer jusqu'à expiration.
4. Si le paiement est valide avant l'expiration, le système crée la facture payée, la liaison paiement/facture et le mouvement d'encaissement. Les places restent bloquées et la vente reste brouillon pendant la validation de la facture.
5. Si la facture est corrigée puis validée par APJ, la réservation passe à `CONFIRMEE`, la vente à `VALIDEE`, les places à `ACHETEE` et les billets sont émis.
6. Si la facture est définitivement refusée, la réservation et la vente sont annulées immédiatement, les places libérées et le paiement remboursé.
7. Si aucun paiement valide n'arrive avant l'expiration, la réservation passe à `EXPIREE`, la vente à `ANNULEE` et les places sont libérées. Aucun paiement tardif ne la confirme.

### Flux B — Vente directe

1. Le client/agent choisit une séance et les places disponibles; le service les bloque dans une transaction.
2. Après paiement valide, le système crée la facture payée, la liaison paiement/facture et le mouvement d'encaissement. Les places restent bloquées pendant la validation de la facture.
3. Après validation APJ de la facture, la vente devient définitive et les billets sont émis.
4. Si la facture est corrigeable, elle est corrigée et soumise à nouveau; les places restent bloquées.
5. Si elle est définitivement refusée, la vente est annulée, les places libérées et le paiement remboursé.

### Flux C — Remboursement demandé par le client

1. Le client demande un remboursement total ou par billet avant le début de chaque séance concernée.
2. Le superutilisateur approuve ou refuse la demande. L'heure de dépôt fait foi même si la demande est traitée après le début de la séance.
3. Si elle est approuvée, le système annule les billets concernés, libère les places, crée un avoir lié à la facture d'origine et crée le mouvement de décaissement.
4. Si elle est refusée, la vente reste valable; une réservation payée dont la facture attendait sa validation est confirmée par validation de la facture.

### Flux D — Validation des mouvements de caisse

1. Chaque paiement valide crée un mouvement d'encaissement.
2. Le superutilisateur valide manuellement le mouvement. Cette validation ne bloque pas les billets.
3. En cas de refus, le mouvement est corrigé et soumis à nouveau, ou annulé et remplacé par un mouvement lié à l'original. La vente reste valable.

## 4. Scripts SQL

Les scripts de base de données sont prévus dans:

- `sql/framework-base.sql` (socle APJ)
- `sql/cinema-application-final.sql` (métier cinéma et commercial)

Ordre d'exécution recommandé: d'abord le socle APJ, ensuite le schéma cinéma. Le script final doit rester cohérent avec ces règles et le comportement APJ réel.
