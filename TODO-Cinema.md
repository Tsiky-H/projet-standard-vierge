# TODO - Application Cinema APJ

Ce fichier contient le backlog complet d'implementation de l'application
cinema dans le projet vierge.

## Legende

- `[ ]` a faire
- `[~]` en cours
- `[x]` termine
- `[!]` bloque

Les cases indiquent l'avancement courant; les taches cochees sont deja
realisees et les taches marquees `[~]` restent en cours.

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

- [ ] **Implementer le service de reservation**
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
  - `ReservationService` est actuellement un squelette vide. Recréer
    `creerReservation` avec verrouillage ordonné des places, snapshot des prix,
    réservation/détails/places bloquées et vente brouillon liée.
  - L'expiration doit vérifier le paiement valide à temps par
    `CINE_PAIEMENT.IDVENTE`, puis `VENTE.IDRESERVATION`; elle doit aussi annuler
    la vente brouillon quand la réservation expire.
  - Le paiement et l'annulation à la demande restent à implémenter.
  - Depend de: `creating-reservation-classes`,
    `implementing-showing-availability-service`.

- [ ] **Implementer le service d'expiration**
  - ID: `implementing-expiration-service`
  - Fournir un traitement administratif pour expirer les reservations sans
    paiement valide a temps, liberer logiquement les places et refuser les
    paiements tardifs. Le traitement transactionnel d'expiration reste à
    implémenter dans `ReservationService`.
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
    des prix, date/heure de vente (`Timestamp`), facture et paiement rattache a
    la vente. Apres paiement valide, passer la vente a `VALIDEE`, acheter les
    places et emettre les billets dans la meme transaction, sans attendre la
    validation APJ de la facture.
  - Si la facture est corrigeable, la corriger et la soumettre a nouveau sans
    changer la vente deja validee. Si elle est definitivement refusee, annuler
    la vente, les billets et liberer les places, puis rembourser le paiement.
  - Parcours initial implemente: creation transactionnelle de vente et lignes,
    verrouillage des places, snapshot des prix, paiement, facture, mouvement
    de caisse, vente validee et billets emis dans la transaction de paiement.
    Restent a traiter le refus/correction definitive, l'annulation et le
    remboursement, puis les essais de concurrence.
  - Depend de: `creating-sales-payment-classes`,
    `implementing-showing-availability-service`.

## 5. Navigation et ecrans

### A faire prochainement

- [x] **Creer les formulaires de creation des trois entites cinema**
  - ID: `creating-film-room-showing-forms`
  - Entites concernees: `Film`, `Salle` et `Seance`. Creer un formulaire APJ
    de creation pour chacune, avec les controles metier associes.
  - **Film:** saisir le titre, la duree en minutes et la classification d'age
    facultative. L'ID genere est l'unique identifiant du film, le titre est
    obligatoire et la duree doit etre strictement positive. Le film est actif
    par defaut. Les colonnes `CODE` redondantes ont ete supprimees des tables
    metier; l'ID est leur identifiant unique.
  - **Salle:** saisir le libelle, puis definir son plan par rangees.
    Chaque categorie active correspond a une rangee; saisir son nombre de places.
    Generer les places avec leur numero, rangee et categorie a partir de cette
    configuration. Calculer la capacite comme le nombre total de places
    generees, sans la saisir une seconde fois. Choisir explicitement l'etat
    actif de la salle.
  - **Seance:** choisir un film actif, une salle active, un jour et une heure
    de debut. Calculer l'heure de fin a partir de la duree du film. A la
    creation, initialiser la seance a `OUVERTE`. Refuser une heure de fin non
    posterieure au debut et tout chevauchement avec une autre seance non
    annulee dans la meme salle.
  - Les reservations sont permises uniquement avant le debut de la seance.
    Apres le debut, les reservations sont fermees, mais l'achat direct reste
    possible jusqu'a la fin si une place est disponible. Determiner l'etat
    `PLEINE` a partir des places disponibles plutot que de le stocker comme
    statut persistant, car les places bloquees peuvent etre liberees.
  - Depend de: `creating-cinema-menu`, `implementing-reference-services`.

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

- [x] **Creer les ecrans de vente directe**
  - ID: `creating-sales-screens`
  - Creer les ecrans de selection des places, paiement, fiche et liste des
    ventes directes. Les ecrans de saisie, plan, paiement, liste et fiche sont
    presents; ajouter ensuite le parcours de refus/correction de facture.
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

------------------------------------------------------------------------------------------------

**Modification/amelioration**(niveau 1-10 du moins important au plus urgent)
-formulaire de creation pour chaque entite(3)
  *Pour chaque entite, decider des regles de gestion et contraintes a respecter lors des creations

-bouton de navigation (de la liste vers le formulaire de creation ou inversement, naviguer entre toutes les pages liste,fiche et saisie pour chaque entite)(3)
  *le clique sur une salle dans la liste aussi redirige vers le plan de la salle
  *decider des flux de navigation logique a avoir

-enrichir le formulaire pour faire une reservation: ajouter champs de saisie pour date expiration meme si facultatif,decider si on separera les champs pour creer une seance(film,heure,salle,jour),et dans ce cas ces champs seront interdependants, et on creera ensuite la seance dans le systeme(6)
  *champs interdependants: si on choisit une salle, la liste de films, jours, heures de diffusions seront mis a jour automatiquement et ainsi de suite a chaque choix

-[x] afficher des valeurs lisibles dans les listes et fiches pour les réservations, ventes et factures (vues SQL dédiées)(7)
  *Vues: CINE_RESERVATION_LIB, CINE_VENTE_LIB et CINE_FACTURECLIENT_LIB; modèles de lecture: ReservationLib, VenteLib et FactureClientLib.

-permettre l'achat directe de billets(creer affichage aussi)(5)

-une reservation et achat directe devront generes des billets ou etre rattaches a des billets? on doit voir aussi ces billets(2)

## 8. Ajouter les métadonnées réelles des films

- [ ] **Ajouter réalisateur et genre aux films**
  - ID: `adding-film-director-and-genre`
  - Enregistrer les informations présentes dans le programme réel :
    `REALISATEUR` et `GENRE` appartiennent à `CINE_FILM`. Ne pas ajouter le
    réalisateur à `CINE_SEANCE`, qui référence déjà le film.
  - Décider les longueurs et les règles de saisie des deux champs, et confirmer
    si le genre reste un texte libre pour la première version ou devient une
    table de référence.
  - **SQL de création et migration** : ajouter les colonnes à
    `CINE_FILM` dans `cinema-application-final.sql`; créer un script de
    migration non destructif pour les bases existantes; renseigner les valeurs
    des trois films réels et répercuter les colonnes dans les vues de lecture.
  - **Classe métier** : ajouter `realisateur` et `genre` à `cinema.Film`, avec
    accesseurs et contrôles de saisie; vérifier l'héritage et le mapping de
    `cinema.FilmLib`.
  - **Vues et classes de lecture** : exposer les champs dans
    `CINE_FILM_LIB` et `CINE_SEANCE_LIB`, puis dans les vues dérivées utilisées
    pour choisir une séance (`CINE_SEANCE_RESERVABLE` et
    `CINE_SEANCE_VENDABLE`). Ajouter les propriétés nécessaires à
    `cinema.SeanceLib` et vérifier les listes APJ.
  - **Formulaire et affichages JSP** : ajouter les champs au formulaire de
    création du film, définir leurs libellés et validations, puis afficher
    réalisateur et genre dans la liste/fiche des films et, si utile, dans les
    choix et affichages des séances.
  - **Données et documentation** : mettre à jour
    `cinema-donnees-reelles-2026.sql`, les scripts de réinitialisation et les
    documents `Vues-Cinema.md`, `Ecrans-Cinema.md` et `README.md`.
  - **Vérifications** : rejouer la migration sur une base contenant déjà des
    données; vérifier le chargement des données réelles, la création de film,
    les listes de films et les sélections de séances; compiler et tester les
    écrans concernés.
  - Depend de: `cinema-donnees-reelles-2026.sql` et de la décision sur le
    modèle de genre.

