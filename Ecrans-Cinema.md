# Workflow des ecrans - Application Cinema APJ

## 1. Principe general d'integration

Le layout existant du projet standard est conserve. Les pages cinema ne
doivent pas reconstruire le HTML global de l'application.

Le point d'entree reste:

```text
module.jsp?but=<chemin-de-la-page-cinema>
```

Le fichier `module.jsp` conserve la responsabilite de:

- verifier la session utilisateur;
- afficher le header;
- afficher le menu gauche;
- inclure la page demandee dans `#butjsp`;
- charger les scripts et le footer communs.

Chaque page cinema fournit uniquement son contenu interne, en reprenant les
conventions existantes:

```html
<div class="content-wrapper">
    <section class="content-header">...</section>
    <section class="content">...</section>
</div>
```

Les classes APJ `PageRecherche`, `PageConsulte`, `PageInsert` et
`PageInsertMultiple` generent les formulaires et tableaux. Les JSP ne doivent
pas remplacer le layout global ni recopier `header.jsp`, `menu/module.jsp`,
`footer.jsp` ou `elements/css.jsp`.

Pour les opérations qui nécessitent un contrôle métier, les pages de création
doivent passer par `UserEJB.createObject`, qui déclenche `controler(c)` avant
l'insertion. Les pages de modification doivent passer par
`UserEJB.updateObjectValidation`, qui déclenche `controlerUpdate(c)` avant
l'écriture sur la même connexion. `UserEJB.updateObject` seul ne déclenche pas
ce contrôle. Aucune page JSP cinéma n'étant encore créée, ce routage devra être
respecté lors de leur implémentation.

## 2. Menu cinema

Fichier de configuration:

- `sql/cinema-menu.sql` (a creer lors de l'implementation du menu)

Entrees proposees:

```text
Cinema
  Salles
  Categories de places
  Films
  Seances
Reservations
  Nouvelle reservation
  Liste des reservations
Ventes
  Vente directe
  Liste des ventes
Facturation
  Factures clients
  Avoirs
Caisse
  Caisses
  Mouvements de caisse
```

Le menu ouvre toujours une page avec `module.jsp?but=...`. Il ne doit pas
ouvrir directement une JSP metier hors du layout.

## 3. Ecran obligatoire: liste des salles

### 3.1 Fichier et classe

- JSP: `arbiochem-war/web/pages/cinema/salle/salle-liste.jsp`
- Classe: `cinema.CineSalle`
- Page APJ: `PageRecherche`

### 3.2 Ouverture

```text
Menu Cinema > Salles
ou
module.jsp?but=cinema/salle/salle-liste.jsp
```

### 3.3 Disposition

Dans le contenu central:

```text
Titre: Liste des salles                         [Nouvelle salle]

Filtres:
  Code salle | Libelle | Active                         [Rechercher]

Tableau:
  Code | Libelle | Capacite | Nombre de places | Etat | Actions
```

La colonne `Libelle` ou `Code` est un lien vers la fiche de la salle.
La colonne `Actions` contient au minimum:

- `Voir la salle`;
- `Voir le plan`;
- `Modifier` si l'utilisateur dispose du droit.

La liste doit utiliser `PageRecherche` et le tableau APJ, comme les listes
existantes du projet standard. Il ne faut pas construire un tableau HTML
independant sauf si le framework l'impose pour le plan graphique.

### 3.4 Action d'un clic

| Clic | Destination | Action |
|---|---|---|
| `Nouvelle salle` | `cinema/salle/salle-saisie.jsp` | Ouvre le formulaire de creation |
| Une salle | `cinema/salle/salle-fiche.jsp?id=...` | Charge la salle et ses places |
| `Voir le plan` | `cinema/salle/salle-plan.jsp?id=...` | Charge le plan et les places de la salle |
| `Modifier` | `cinema/salle/salle-saisie.jsp?acte=update&id=...` | Prepare la modification |
| `Rechercher` | Meme JSP | Reexecute les criteres `PageRecherche` |

## 4. Ecran obligatoire: fiche et plan d'une salle

### 4.1 Fichiers et classes

- JSP fiche: `arbiochem-war/web/pages/cinema/salle/salle-fiche.jsp`
- JSP plan: `arbiochem-war/web/pages/cinema/salle/salle-plan.jsp`
- Classe mere: `cinema.CineSalle`
- Classe fille: `cinema.CinePlace`
- Vue de lecture recommandee: `cinema.CinePlaceLib`
- Page APJ: `PageConsulte` pour la fiche, tableau APJ pour les places

### 4.2 Ouverture

```text
Depuis la liste:
module.jsp?but=cinema/salle/salle-fiche.jsp&id=<IDSALLE>
```

La fiche affiche les informations de la salle et deux onglets:

```text
[Informations] [Plan des places]
```

Le changement d'onglet reste dans la meme fiche:

```text
module.jsp?but=cinema/salle/salle-fiche.jsp&id=<IDSALLE>&tab=plan
```

### 4.3 Disposition de la fiche

```text
< Retour a la liste des salles       Fiche de la salle

Bloc Informations:
  Code
  Libelle
  Capacite
  Etat

Actions:
  Modifier la salle | Ajouter une place | Voir le plan

Onglets:
  Informations | Plan des places
```

`PageConsulte` affiche les informations. Les actions sont des liens APJ
classiques avec retour vers la fiche apres traitement.

### 4.4 Disposition du plan

Le plan doit etre lisible et distinguer visuellement les categories de
places. Le plan est le seul composant qui peut necessiter un rendu HTML
interne specifique, car le framework ne connait pas la geometrie d'une salle.
Il reste cependant contenu dans le `content-wrapper` de la JSP.

```text
Fiche de la salle: Salle 1
Capacite: 120 places

Legende:
  [Bleu] Standard - 15 000
  [Or]   VIP      - 30 000
  [Gris] Inactive

                    ECRAN

Rangee A: [A01] [A02] [A03] [A04] [A05]
Rangee B: [B01] [B02] [B03] [B04] [B05]
Rangee C: [C01] [C02] [C03] [C04] [C05]

Tableau detail:
  Numero | Rangee | Categorie | Prix | Etat | Action
```

Chaque place affiche obligatoirement:

- son numero;
- sa categorie par couleur/label;
- son etat active/inactive;
- son prix de categorie.

Le tableau detail reste disponible sous le plan pour permettre la recherche,
le controle et la modification par un administrateur.

### 4.5 Actions d'un clic sur le plan

| Clic | Destination/traitement | Action |
|---|---|---|
| Une place | `cinema/place/place-fiche.jsp?id=...` | Affiche le detail de la place |
| `Ajouter une place` | `cinema/place/place-saisie.jsp?idsalle=...` | Cree une place dans la salle |
| `Modifier` sur une place | `cinema/place/place-saisie.jsp?acte=update&id=...` | Modifie numero/rangee/categorie |
| `Desactiver` | `cinema/place/place-action.jsp` | Desactive sans supprimer l'historique |
| `Retour` | `cinema/salle/salle-liste.jsp` | Revient a la liste |

Une place deja utilisee dans une reservation ou une vente ne doit pas etre
supprimee physiquement. Elle est desactivee afin de conserver l'historique.

## 5. Ecrans de referentiel

### 5.1 Categories de places

Fichiers:

- `cinema/categorie-place/categorie-place-liste.jsp`
- `cinema/categorie-place/categorie-place-saisie.jsp`
- `cinema/categorie-place/categorie-place-fiche.jsp`
- Classe: `cinema.CineCategoriePlace`

Parcours:

```text
Liste > Nouvelle categorie > Enregistrer > Fiche categorie
Liste > Categorie > Fiche > Modifier > Enregistrer > Fiche categorie
```

La categorie contient `code`, `libelle`, `prix_base`, `actif`. Une modification
du prix ne modifie jamais les anciens billets: le prix est copie dans les
lignes de reservation/vente.

### 5.2 Films

Fichiers:

- `cinema/film/film-liste.jsp`
- `cinema/film/film-saisie.jsp`
- `cinema/film/film-fiche.jsp`
- Classe: `cinema.CineFilm`

Actions:

```text
Liste films > Nouveau film > Enregistrer > Fiche film
Fiche film > Voir les seances
Fiche film > Modifier
```

### 5.3 Seances

Fichiers:

- `cinema/seance/seance-liste.jsp`
- `cinema/seance/seance-saisie.jsp`
- `cinema/seance/seance-fiche.jsp`
- Classe APJ: `cinema.Seance`
- Contrôles métier: `controler(c)` à la création et `controlerUpdate(c)` à la modification

Dans `seance-saisie.jsp`, l'utilisateur choisit:

```text
Film | Salle | Date/heure debut | Date/heure fin
```

Au clic sur `Enregistrer`, APJ appelle le contrôle de la classe `Seance`. Celui-ci
verrouille la salle et vérifie qu'aucune autre séance non annulée ne chevauche
l'intervalle. À la modification, le contrôle exclut la séance elle-même. En cas
de chevauchement, l'enregistrement est refusé avec un message explicite et
l'utilisateur reste sur le formulaire.

## 6. Flux reservation

### 6.1 Ecrans et fichiers

- `reservation/reservation-liste.jsp`
- `reservation/reservation-saisie.jsp`
- `reservation/reservation-places.jsp`
- `reservation/reservation-paiement.jsp`
- `reservation/reservation-fiche.jsp`
- `reservation/reservation-expiration.jsp`
- Classes:
  - `reservation.CineReservation`
  - `reservation.CineReservationDetail`
  - `reservation.CineReservationPlace`
- Service: `service.ReservationService`
- Service: `service.PaiementService`

### 6.2 Parcours utilisateur

```text
Nouvelle reservation
  -> choisir client
  -> choisir une seance
  -> afficher le plan de la salle
  -> cliquer sur les places disponibles
  -> ajouter une autre seance (optionnel)
  -> choisir le mode de paiement prevu
  -> creer la reservation
  -> afficher le recapitulatif et la date d'expiration
  -> payer
  -> paiement valide: creer la facture payee et le mouvement d'encaissement
  -> attendre la validation APJ de la facture
  -> apres validation: confirmer la reservation et emettre les billets
```

### 6.3 Detail des actions

1. `reservation-saisie.jsp` affiche client, seance et bouton `Choisir les
   places`.
2. `reservation-places.jsp` affiche le plan de la salle de la seance.
3. Les places deja vendues ou bloquees sont non selectionnables.
4. Le clic sur `Continuer` valide temporairement la selection en session
   HTTP; aucune reservation n'est encore consideree comme confirmee.
5. `Ajouter une seance` permet de recommencer la selection pour une autre
   seance dans la meme reservation mere.
6. `Valider la reservation` appelle `ReservationService` dans une transaction:
   creation de la mere, des details, des lignes places et de l'expiration.
7. Le systeme redirige vers `reservation-fiche.jsp?id=...`.
8. `Payer` ouvre `reservation-paiement.jsp`.
9. `PaiementService` enregistre le paiement valide et cree la facture payee,
   la liaison paiement/facture et le mouvement d'encaissement.
10. La reservation et les places restent en attente pendant la validation
    manuelle APJ de la facture. Le mouvement de caisse suit son propre
    workflow et ne bloque pas les billets.
11. Apres validation de la facture, la reservation passe `CONFIRMEE`, les
    places passent `ACHETEE` et les billets sont emis.
12. Si la facture est corrigeable, elle est corrigee et soumise a nouveau;
    la date d'expiration initiale reste inchangee. Une reservation deja payee
    a temps n'expire pas pendant l'attente interne.
13. Si la facture est definitivement refusee, la reservation est annulee sans
    attendre l'expiration, les places sont liberees et le paiement est
    rembourse.

## 7. Flux vente directe

Fichiers:

- `vente/vente-directe-saisie.jsp`
- `vente/vente-paiement.jsp`
- `vente/vente-fiche.jsp`
- `vente/vente-liste.jsp`
- Classes: `vente.Vente`, `vente.VenteDetails`
- Services: `service.VenteService`, `service.PaiementService`

Parcours:

```text
Vente directe
  -> choisir film/seance
  -> choisir les places sur le plan
  -> verifier le montant
  -> choisir le mode de paiement
  -> payer
  -> creer facture payee + mouvement d'encaissement
  -> attendre la validation APJ de la facture, places bloquees
  -> valider la facture
  -> emettre les billets
  -> afficher la vente
```

La reservation temporaire n'est pas necessaire pour une vente directe, mais
les places sont bloquees apres paiement jusqu'a la validation de la facture.
Une facture corrigeable est soumise a nouveau sans liberer les places. Si elle
est definitivement refusee, la vente est annulee, les places sont liberees et
le paiement est rembourse.

## 8. Facture, paiement et caisse

Fichiers:

- `facture/factureclient-liste.jsp`
- `facture/factureclient-fiche.jsp`
- `paiement/paiement-liste.jsp`
- `caisse/caisse-liste.jsp`
- `caisse/mvt/mvtcaisse-liste.jsp`

Apres un paiement valide:

```text
Paiement
  -> FactureClient
  -> LiaisonPaiement
  -> MvtCaisse
  -> attente de validation manuelle de la facture
  -> apres validation facture: billets
```

Un paiement refuse n'est pas un refus de facture: aucun encaissement n'est
cree et la reservation reste en attente jusqu'a son expiration. Si le paiement
est valide a temps, la reservation n'expire plus pendant l'attente de la
validation de facture.

Les factures et mouvements de caisse affichent separement le `STATUT` metier
et l'`ETAT` de validation APJ. La validation du mouvement d'encaissement ne
bloque pas la confirmation ou les billets.

### 8.1 Remboursement et avoir

Le client peut demander un remboursement complet ou par billet avant le debut
de chaque seance concernee. Pour une reservation multi-seances, chaque billet
est controle selon sa propre seance. L'heure de depot de la demande fait foi:
une demande deposee a temps reste recevable meme si le superutilisateur la
traite apres le debut; une demande deposee apres le debut est refusee.

Le superutilisateur approuve ou refuse la demande. A l'approbation, le systeme
annule les billets concernes, libere les places et cree automatiquement un
avoir dans `FACTURECLIENT`, lie a la facture d'origine, ainsi qu'un mouvement
de caisse `DECAISSEMENT`. La facture d'origine validee est conservee sans
modification. Si la demande est refusee, la vente reste valable; une
reservation payee dont la facture attend sa validation est confirmee par la
validation de cette facture.

Un mouvement de caisse refuse est corrige puis soumis a nouveau s'il peut
l'etre. Sinon, il est annule sans suppression et remplace par un nouveau
mouvement lie a l'original; cela ne remet pas en cause la vente payee.

## 9. Expiration des reservations

Fichier/service:

- `service.ReservationExpirationService`
- `reservation/reservation-expiration.jsp` (ecran administratif de controle)

Avant chaque paiement, et lors d'une consultation de reservation en attente:

1. comparer `SYSDATE` a `DATE_EXPIRATION`;
2. si la date est depassee et qu'aucun paiement valide n'a ete recu a temps,
   passer la reservation a `EXPIREE`;
3. passer les places `BLOQUEE` a `EXPIREE`;
4. refuser le paiement tardif;
5. permettre une nouvelle reservation.

Un traitement periodique peut nettoyer les reservations expirees, mais il ne
remplace pas ce controle avant paiement.

Une reservation payee avant l'expiration ne doit pas etre expiree pendant
l'attente d'une validation interne de facture. Une facture definitivement
refusee annule la reservation et libere les places immediatement.

## 10. Arborescence cible des ecrans

```text
arbiochem-war/web/pages/
  cinema/
    salle/
      salle-liste.jsp
      salle-saisie.jsp
      salle-fiche.jsp
      salle-plan.jsp
    place/
      place-saisie.jsp
      place-fiche.jsp
      place-action.jsp
    categorie-place/
      categorie-place-liste.jsp
      categorie-place-saisie.jsp
      categorie-place-fiche.jsp
    film/
      film-liste.jsp
      film-saisie.jsp
      film-fiche.jsp
    seance/
      seance-liste.jsp
      seance-saisie.jsp
      seance-fiche.jsp
  reservation/
    reservation-liste.jsp
    reservation-saisie.jsp
    reservation-places.jsp
    reservation-paiement.jsp
    reservation-fiche.jsp
    reservation-expiration.jsp
  vente/
    vente-directe-saisie.jsp
    vente-paiement.jsp
    vente-fiche.jsp
    vente-liste.jsp
```

## 11. Regle de preservation du layout

Les futurs fichiers JSP doivent modifier uniquement:

- les imports Java;
- la preparation des objets APJ;
- la configuration des champs et liens;
- le contenu interieur de `content-wrapper`;
- les appels aux services metier.

Les fichiers suivants restent ceux du framework/layout et ne sont pas
recopies dans les pages cinema:

- `module.jsp`;
- `elements/header.jsp`;
- `elements/menu/module.jsp`;
- `elements/footer.jsp`;
- `elements/css.jsp`;
- `elements/js.jsp`.
