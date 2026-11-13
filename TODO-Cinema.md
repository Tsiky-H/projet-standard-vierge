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
  - Verifier les dependances de workflow APJ `HISTORIQUEETAT` et
    `getSeqHistoriqueEtat` utilisees par `ClassEtat`.
  - Depend de: `configuring-cinema-project`.

- [ ] **Installer le schema cinema**
  - ID: `installing-cinema-schema`
  - Executer et verifier
    [sql/cinema-application-final.sql](./sql/cinema-application-final.sql)
    apres le schema du framework. Verifier tables, contraintes, sequences et
    donnees initiales.
  - Depend de: `installing-framework-schema`.

- [ ] **Valider le modele de donnees cinema**
  - ID: `validating-cinema-database-model`
  - Verifier les cles etrangeres, contraintes d'unicite, statuts, snapshots
    de prix, unicite d'une place par seance et index.
  - Le script impose maintenant un paiement rattaché à `VENTE.IDVENTE`; pour
    une réservation, `VENTE.IDRESERVATION` donne la réservation source.
    L'expiration doit suivre ce lien.
  - `VENTE.IDCAISSE` est nullable uniquement pour une vente de réservation
    `BROUILLON`; la caisse active est affectée à l'enregistrement du paiement.
    `Vente.controler(c)` applique cette règle à la création.
  - Prevoir `ETAT` uniquement pour les factures et mouvements de caisse qui
    utilisent la validation APJ; conserver `STATUT` pour les etats metier.
  - Representer un avoir comme un document dans `FACTURECLIENT`, avec un type
    distinct et une reference vers la facture d'origine. Ne pas modifier ni
    supprimer une facture deja validee.
  - Creer automatiquement le mouvement `DECAISSEMENT` lors de l'approbation
    d'un remboursement; conserver le lien au paiement et a l'avoir.
  - Ne pas ajouter `IDUSER` pour filtrer les donnees: la premiere version a
    un superutilisateur unique qui accede a tout.
  - Depend de: `installing-cinema-schema`.

## 3. Classes Java APJ

- [x] **Créer les 19 modèles APJ du schéma cinéma**
  - ID: `creating-cinema-models`
  - Inventaire complet des classes, tables et classes parentes : section 2 de
    [Cinema.md](./Cinema.md).
  - `cinema.Salle`, `client.Client`, `caisse.Caisse`, `caisse.MvtCaisse` et
    `paiement.ModePaiement` étaient déjà présents; les 14 autres modèles sont créés.
  - Les références, réservations, ventes, paiements et lignes simples héritent
    de `ClassMAPTable`; `FactureClient` héritera de `ClassMere` et
    `FactureClientLigne` de `ClassFille`. `MvtCaisse` existe déjà et hérite de
    `ClassEtat`.
  - Les réservations et ventes n'ont pas de colonne `ETAT`; leurs opérations
    mère/détail/place sont donc coordonnées par les services métier, pas par
    `ClassMere`/`ClassFille`.
  - Les 12 modèles simples et les modèles `FactureClient` / `FactureClientLigne`
    sont créés. Les lignes de facture ont une colonne `ETAT` pour le workflow
    APJ mère/fille.
  - Dépend de: `validating-cinema-database-model`.

- [x] **Adapter le socle Java APJ**
  - ID: `adapting-apj-java-core`
  - Intégrer `UserEJB`, `UserEJBBean`, `CreateObject`, `UpdateObject` et
    `ValiderObject`; retirer les accesseurs et traitements dépendant de
    Societe, Magasin, Point, paie CNAPS et chatbot.
  - Le détail des classes d'infrastructure est dans
    [Cinema.md](./Cinema.md), section 2.5.

## 4. Services metier

- [ ] **Implementer les services des referentiels**
  - ID: `implementing-reference-services`
  - Implementer les validations et la persistance des salles, places,
    categories, films et seances, y compris les etats actifs/inactifs.
  - Depend de: `creating-cinema-reference-classes`.

- [x] **Controler les chevauchements de seances**
  - ID: `implementing-showing-availability-service`
  - `Seance.controler(c)` et `Seance.controlerUpdate(c)` refusent les
    chevauchements dans une meme salle et verrouillent la salle pendant le
    controle. APJ les appelle dans la transaction de creation/modification.

- [~] **Implementer le service de reservation**
  - ID: `implementing-reservation-service`
  - Implementer le controle atomique des places, la reservation mere/detail/
    place, la date limite de paiement, le snapshot du prix et les reservations
    contenant plusieurs seances.
  - Un paiement valide avant l'expiration empêche l'expiration automatique
    pendant l'attente de validation de la facture. Une facture définitivement
    refusee annule immediatement la reservation, libere les places et declenche
    le remboursement; une facture corrigeable peut etre soumise a nouveau.
  - Permettre l'annulation immediate d'une reservation non payee avant son
    expiration.
  - Le code actuel de `ReservationService.creerReservation` crée la réservation,
    ses détails et ses places bloquées dans une transaction, avec verrouillage
    ordonné et snapshot des prix. Il reste à y créer la vente brouillon liée.
  - Le code d'expiration transactionnelle doit aussi être adapté à la nouvelle
    règle : vérifier le paiement valide à temps par `CINE_PAIEMENT.IDVENTE` puis
    `VENTE.IDRESERVATION`, et annuler la vente brouillon quand la réservation
    expire.
  - Le paiement et l'annulation à la demande restent à implémenter.
  - Depend de: `creating-reservation-classes`,
    `implementing-showing-availability-service`.

- [ ] **Implementer le service d'expiration**
  - ID: `implementing-expiration-service`
  - Fournir un traitement administratif pour expirer les reservations sans
    paiement valide a temps, liberer logiquement les places et refuser les
    paiements tardifs. L'expiration transactionnelle des reservations qui
    occupent les places demandees est deja geree par `ReservationService`.
  - Depend de: `implementing-reservation-service`.

- [ ] **Implementer le service de paiement**
  - ID: `implementing-payment-service`
  - Enregistrer les paiements valides/refuses en les rattachant toujours à
    `VENTE.IDVENTE`; pour une réservation, retrouver la source par
    `VENTE.IDRESERVATION`. Un paiement valide cree une facture payee, la liaison
    paiement/facture et un mouvement d'encaissement.
  - Confirmer une reservation ou finaliser une vente seulement apres paiement
    valide et facture validee par APJ. La validation du mouvement de caisse ne
    bloque pas l'emission des billets.
  - Gerer les demandes de remboursement totales ou par billet, soumises a
    l'approbation du superutilisateur. A l'approbation, creer l'avoir lie a la
    facture d'origine et le mouvement de decaissement; une demande refusee
    maintient la vente, et une reservation payee est confirmee par validation
    de sa facture.
  - Recevoir les demandes avant le debut de la seance du billet concerne;
    l'heure de depot fait foi meme si le superutilisateur traite la demande
    apres le debut.
  - Depend de: `creating-sales-payment-classes`,
    `implementing-reservation-service`, `implementing-expiration-service`.

- [ ] **Implementer le service de vente directe**
  - ID: `implementing-direct-sale-service`
  - Creer la vente brouillon avant la tentative de paiement; implementer la
    vente transactionnelle de billets avec verrouillage des places, snapshot
    des prix, facture et paiement rattache a la vente. Garder les places bloquees
    jusqu'a validation APJ de la facture; emettre les billets ensuite.
  - Si la facture est corrigeable, conserver le blocage pendant sa
    correction. Si elle est definitivement refusee, annuler la vente, liberer
    les places et rembourser le paiement.
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
  - Pour une modification soumise a un controle metier, utiliser
    `UserEJB.updateObjectValidation` afin que `controlerUpdate(c)` soit appele
    avant l'ecriture; `updateObject` seul ne declenche pas ce controle.
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
  - Ajouter les actions de validation/correction APJ des factures et
    mouvements; afficher le statut metier separement de l'etat APJ.
  - Permettre la consultation d'un avoir avec sa facture d'origine et des
    mouvements d'encaissement/decaissement lies.
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
    multi-seances, l'expiration, les paiements refuses/tardifs, les validations
    APJ, les factures corrigees/refusees, les remboursements partiels/totaux,
    les avoirs, les mouvements de caisse et l'emission/annulation des billets.
  - Verifier qu'une reservation payee a temps n'expire pas pendant une attente
    de validation interne et qu'une demande de remboursement deposee avant la
    seance reste recevable si elle est traitee apres son debut.
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
- [sql/cinema-application-final.sql](./sql/cinema-application-final.sql)
