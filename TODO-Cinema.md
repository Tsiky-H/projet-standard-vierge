# TODO - Application Cinema APJ

Ce fichier contient le backlog complet d'implementation de l'application
cinema dans le projet vierge.

## Legende

- `[ ]` a faire
- `[~]` en cours
- `[x]` termine
- `[!]` bloque

Toutes les taches sont actuellement a faire.

## Ordre general

```text
Conventions techniques
        |
        v
Configuration du projet
        |
        v
Schema framework APJ
        |
        v
Schema cinema
        |
        v
Validation du modele
        |
        +--> Classes APJ
        |       |
        |       v
        |   Services metier
        |       |
        |       v
        |   Ecrans JSP
        |
        v
Tests
        |
        v
Documentation finale
```

## 1. Preparation et conventions

- [ ] **Definir les conventions techniques cinema**
  - ID: `defining-cinema-technical-conventions`
  - Fixer les packages Java, les noms de classes APJ, les mappings avec les
    tables SQL, la generation des identifiants, les constantes de statut et
    les conventions partagees.
  - Dependances: aucune.

- [ ] **Configurer le projet cinema**
  - ID: `configuring-cinema-project`
  - Modifier `project.properties`, `apj.properties`, les parametres du build,
    les libelles de l'application et les valeurs de deploiement.
  - Depend de: `defining-cinema-technical-conventions`.

## 2. Base de donnees

- [ ] **Installer le schema du framework**
  - ID: `installing-framework-schema`
  - Executer et verifier [sql/framework-base.sql](./sql/framework-base.sql)
    dans le schema Oracle cible, puis modifier le mot de passe administrateur
    par defaut.
  - Depend de: `configuring-cinema-project`.

- [ ] **Installer le schema cinema**
  - ID: `installing-cinema-schema`
  - Executer et verifier
    [sql/cinema-application-complete.sql](./sql/cinema-application-complete.sql)
    apres le schema du framework. Verifier tables, contraintes, sequences et
    donnees initiales.
  - Depend de: `installing-framework-schema`.

- [ ] **Valider le modele de donnees cinema**
  - ID: `validating-cinema-database-model`
  - Verifier les cles etrangeres, contraintes d'unicite, statuts, snapshots
    de prix, unicite d'une place par seance et index.
  - Depend de: `installing-cinema-schema`.

## 3. Classes Java APJ

- [ ] **Creer les classes de reference cinema**
  - ID: `creating-cinema-reference-classes`
  - Creer les classes APJ `CineSalle`, `CineCategoriePlace`, `CinePlace`,
    `CineFilm` et `CineSeance` avec les conventions `ClassMAPTable`.
  - Depend de: `validating-cinema-database-model`.

- [ ] **Creer les classes commerciales cinema**
  - ID: `creating-cinema-commercial-classes`
  - Creer ou adapter les classes `Client`, `ModePaiement`, `Caisse`,
    `FactureClient`, `FactureClientLigne` et `MvtCaisse`.
  - Depend de: `validating-cinema-database-model`.

- [ ] **Creer les classes de reservation**
  - ID: `creating-reservation-classes`
  - Creer `CineReservation`, `CineReservationDetail` et
    `CineReservationPlace`, avec les relations mere/fille et la gestion des
    statuts.
  - Depend de: `creating-cinema-reference-classes`.

- [ ] **Creer les classes de vente et paiement**
  - ID: `creating-sales-payment-classes`
  - Creer `Vente`, `VenteDetails`, `CinePaiement`, `LiaisonPaiement` et
    `CineBillet`, avec leurs mappings et relations.
  - Depend de: `creating-cinema-commercial-classes`.

## 4. Services metier

- [ ] **Implementer les services des referentiels**
  - ID: `implementing-reference-services`
  - Implementer les validations et la persistance des salles, places,
    categories, films et seances, y compris les etats actifs/inactifs.
  - Depend de: `creating-cinema-reference-classes`.

- [ ] **Implementer le service de disponibilite des seances**
  - ID: `implementing-showing-availability-service`
  - Interdire les seances qui se chevauchent dans une meme salle, avec un
    controle transactionnel et des erreurs explicites.
  - Depend de: `implementing-reference-services`.

- [ ] **Implementer le service de reservation**
  - ID: `implementing-reservation-service`
  - Implementer le controle atomique des places, la reservation mere/detail/
    place, la date d'expiration, le snapshot du prix et les reservations
    contenant plusieurs seances.
  - Depend de: `creating-reservation-classes`,
    `implementing-showing-availability-service`.

- [ ] **Implementer le service d'expiration**
  - ID: `implementing-expiration-service`
  - Expirer les reservations non payees, liberer logiquement les places,
    refuser les paiements expires et fournir un controle administratif.
  - Depend de: `implementing-reservation-service`.

- [ ] **Implementer le service de paiement**
  - ID: `implementing-payment-service`
  - Valider ou refuser les paiements, confirmer les reservations uniquement
    apres paiement valide, relier paiement/facture et creer les mouvements
    de caisse.
  - Depend de: `creating-sales-payment-classes`,
    `implementing-reservation-service`, `implementing-expiration-service`.

- [ ] **Implementer le service de vente directe**
  - ID: `implementing-direct-sale-service`
  - Implementer la vente transactionnelle de billets avec verrouillage des
    places, snapshot des prix, facture, paiement, caisse et emission des
    billets.
  - Depend de: `creating-sales-payment-classes`,
    `implementing-showing-availability-service`.

## 5. Navigation et ecrans

- [ ] **Creer le menu cinema**
  - ID: `creating-cinema-menu`
  - Ajouter les entrees et droits pour les salles, categories, films,
    seances, reservations, ventes, factures et caisse.
  - Le menu doit utiliser `module.jsp?but=...`.
  - Depend de: `configuring-cinema-project`.

- [ ] **Creer les ecrans de gestion des salles**
  - ID: `creating-hall-screens`
  - Creer:
    - `cinema/salle/salle-liste.jsp`
    - `cinema/salle/salle-saisie.jsp`
    - `cinema/salle/salle-fiche.jsp`
    - `cinema/salle/salle-plan.jsp`
    - `cinema/place/place-fiche.jsp`
    - `cinema/place/place-saisie.jsp`
    - `cinema/place/place-action.jsp`
  - Utiliser le layout APJ existant sans modifier `module.jsp`.
  - Depend de: `creating-cinema-menu`,
    `implementing-reference-services`.

- [ ] **Creer les ecrans de referentiel cinema**
  - ID: `creating-reference-screens`
  - Creer les ecrans liste/saisie/fiche des categories, films et seances avec
    `PageRecherche`, `PageInsert`, `PageConsulte` et les liens APJ.
  - Depend de: `creating-cinema-menu`,
    `implementing-reference-services`.

- [ ] **Creer les ecrans de reservation**
  - ID: `creating-reservation-screens`
  - Creer:
    - `reservation/reservation-liste.jsp`
    - `reservation/reservation-saisie.jsp`
    - `reservation/reservation-places.jsp`
    - `reservation/reservation-paiement.jsp`
    - `reservation/reservation-fiche.jsp`
    - `reservation/reservation-expiration.jsp`
  - Depend de: `creating-hall-screens`,
    `implementing-reservation-service`,
    `implementing-payment-service`.

- [ ] **Creer les ecrans de vente directe**
  - ID: `creating-sales-screens`
  - Creer les ecrans de selection des places, paiement, fiche et liste des
    ventes directes.
  - Depend de: `creating-hall-screens`,
    `implementing-direct-sale-service`.

- [ ] **Creer les ecrans facture, paiement et caisse**
  - ID: `creating-invoice-payment-cash-screens`
  - Creer ou adapter les ecrans de facture client, paiement, caisse et
    mouvements de caisse pour assurer la tracabilite des operations cinema.
  - Depend de: `creating-sales-payment-classes`,
    `implementing-payment-service`.

- [ ] **Implementer le rendu du plan de salle**
  - ID: `implementing-seat-plan-rendering`
  - Afficher les rangees, numeros des places, legende des categories, prix,
    etats actifs/inactifs et disponibilites, sans remplacer le layout global.
  - Depend de: `creating-hall-screens`,
    `creating-reservation-screens`, `creating-sales-screens`.

- [ ] **Ajouter les messages de validation cinema**
  - ID: `adding-cinema-validation-messages`
  - Ajouter les libelles, erreurs, confirmations et statuts dans les fichiers
    de ressources localises.
  - Depend de: `creating-reference-screens`,
    `creating-reservation-screens`, `creating-sales-screens`.

## 6. Tests

- [ ] **Tester la base de donnees cinema**
  - ID: `testing-cinema-database`
  - Tester les contraintes Oracle, l'integrite referentielle, l'unicite, les
    statuts, les donnees initiales et les cas representatifs.
  - Depend de: `validating-cinema-database-model`.

- [ ] **Tester les workflows cinema**
  - ID: `testing-cinema-workflows`
  - Tester le plan de salle, les conflits de seances, les reservations
    multi-seances, l'expiration, le paiement, la confirmation, la vente
    directe, la facture, la caisse et les billets.
  - Depend de: `creating-invoice-payment-cash-screens`,
    `implementing-seat-plan-rendering`.

- [ ] **Tester la concurrence sur les places**
  - ID: `testing-concurrent-seat-booking`
  - Verifier que deux reservations/ventes simultanees ne peuvent pas obtenir
    la meme place pour la meme seance et que l'erreur est explicite.
  - Depend de: `implementing-reservation-service`,
    `implementing-direct-sale-service`, `testing-cinema-database`.

## 7. Livraison et documentation

- [ ] **Documenter la livraison cinema**
  - ID: `documenting-cinema-delivery`
  - Maintenir synchronises [Cinema.md](./Cinema.md),
    [Ecrans-Cinema.md](./Ecrans-Cinema.md), [README.md](./README.md) et les
    notes d'implementation avec les classes, JSP, SQL et comportements
    effectivement livres.
  - Depend de: `adding-cinema-validation-messages`,
    `testing-cinema-workflows`, `testing-concurrent-seat-booking`.

## Documents de reference

- [README.md](./README.md)
- [Cinema.md](./Cinema.md)
- [Ecrans-Cinema.md](./Ecrans-Cinema.md)
- [sql/framework-base.sql](./sql/framework-base.sql)
- [sql/cinema-application-complete.sql](./sql/cinema-application-complete.sql)
