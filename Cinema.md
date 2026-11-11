# Specification Cinema (APJ)

## 1) Regles de gestion retenues

### 1.1 Salles, places et categories
- Un cinema possede plusieurs salles.
- Une salle contient plusieurs places numerotees.
- Chaque place appartient a une seule categorie.
- Le prix de base d'une place depend uniquement de sa categorie.
- Le prix est copie dans la ligne de reservation/vente au moment de l'operation (historisation du prix applique).

### 1.2 Films et seances
- Un film peut avoir plusieurs seances.
- Une seance se deroule dans une seule salle.
- Une salle ne peut pas avoir deux seances qui se chevauchent (controle applicatif transactionnel).

### 1.3 Reservation
- Une reservation "mere" peut contenir plusieurs details de reservation (plusieurs seances).
- Un detail de reservation contient les places reservees pour une seance.
- Lors de la reservation, chaque place est mise en etat BLOQUEE avec une DATE_EXPIRATION.
- Tant que la reservation n'est pas payee/validee, les places restent bloquees jusqu'a expiration.
- Si la date d'expiration est depassee sans paiement valide, la reservation passe EXPIREE et les places sont liberees.
- Une reservation est CONFIRMEE uniquement apres validation du paiement.

### 1.4 Vente directe
- Une vente directe permet d'acheter des billets sans reservation prealable.
- Les memes contraintes d'unicite de place par seance s'appliquent.
- Une vente directe genere une facture client et un mouvement de caisse.

### 1.5 Paiement, facture et caisse
- Le mode de paiement est choisi:
  - a la creation de reservation (mode prevu),
  - puis confirme/ajuste au paiement reel.
- Chaque paiement valide est rattache a une facture client.
- Chaque paiement valide produit un mouvement d'encaissement en caisse.
- Toute operation commerciale est tracable via facture et caisse.

### 1.6 Contraintes critiques
- Une place ne peut etre vendue/reservee qu'une seule fois par seance.
- Les operations de blocage de places, paiement, confirmation doivent etre transactionnelles.
- Les statuts sont obligatoires et controles (reservation, details, place, paiement, vente, facture).

## 2) Classes APJ a creer

Les noms suivent le style APJ (objets mere/fille + pages de saisie/liste/consultation).

### 2.1 Referentiels cinema
- `cinema.CineSalle`
- `cinema.CineCategoriePlace`
- `cinema.CinePlace`
- `cinema.CineFilm`
- `cinema.CineSeance`

### 2.2 Referentiels commerciaux
- `vente.Client`
- `vente.ModePaiement`
- `vente.Caisse`

### 2.3 Reservation et vente
- `reservation.CineReservation` (Mere)
- `reservation.CineReservationDetail` (Fille)
- `reservation.CineReservationPlace` (Fille de detail)
- `vente.Vente` (Mere)
- `vente.VenteDetails` (Fille)
- `vente.CineBillet`

### 2.4 Facturation et encaissement
- `facturation.FactureClient`
- `facturation.FactureClientLigne`
- `paiement.CinePaiement`
- `paiement.LiaisonPaiement`
- `caisse.MvtCaisse`

### 2.5 Services metier (fortement recommandes)
- `service.ReservationService`
  - bloquer les places
  - verifier disponibilite
  - expirer reservation
  - confirmer reservation apres paiement
- `service.VenteService`
  - vente directe de billets
  - generation facture
- `service.PaiementService`
  - valider/refuser paiement
  - lier paiement/facture
  - generer mouvement caisse

## 3) Flux retenu de l'application

### Flux A - Reservation puis paiement
1. L'utilisateur choisit film + seance + places.
2. Le systeme verifie disponibilite des places en transaction.
3. Creation:
   - reservation mere (EN_ATTENTE, DATE_EXPIRATION),
   - details (par seance),
   - lignes places (BLOQUEE, prix unitaire capture).
4. Le client paie avant expiration.
5. Si paiement VALIDE:
   - reservation -> CONFIRMEE,
   - lignes place -> ACHETEE,
   - generation facture client,
   - liaison paiement/facture,
   - mouvement caisse ENCAISSEMENT,
   - emission des billets.
6. Si paiement non valide et expiration atteinte:
   - reservation -> EXPIREE,
   - lignes place -> EXPIREE (liberees).

### Flux B - Vente directe
1. L'agent choisit seance + places disponibles.
2. Creation d'une vente directe.
3. Creation lignes vente par place.
4. Paiement immediat.
5. Si paiement VALIDE:
   - facture client,
   - liaison paiement/facture,
   - mouvement caisse ENCAISSEMENT,
   - billets emis.

### Flux C - Annulation/remboursement (optionnel v1)
1. Annulation selon regles metier.
2. Creation mouvement inverse en caisse (DECAISSEMENT) si remboursement.
3. Mise a jour des statuts (facture/vente/place/billet).

## 4) Script SQL de l'application

Le SQL complet des tables de l'application est fourni dans:
- `sql/cinema-application-complete.sql`

Ordre recommande d'execution:
1. `sql/framework-base.sql` (socle APJ)
2. `sql/cinema-application-complete.sql` (metier cinema + commercial)
