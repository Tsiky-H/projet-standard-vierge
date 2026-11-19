# Application de gestion de cinéma — socle APJ

Ce projet reprend le socle APJ du projet standard et l'adapte au schéma
cinéma. Les classes métier et les écrans cinéma sont encore en cours de
création; les règles fonctionnelles sont dans `Cinema.md`.

## Contenu conservé

- `build.xml` : compilation Java, intégration des propriétés APJ et création
  du WAR ;
- `lib/` : bibliothèques APJ, Oracle, Servlet et dépendances requises ;
- `arbiochem-ejb/src/conf/jboss.xml` : descripteur EJB ;
- `arbiochem-war/web/WEB-INF/` : descripteurs WAR et fragment APJ ;
- `user/` : contrat et service APJ adaptés, avec les helpers CRUD et workflow ;
- `client.Client`, `caisse.Caisse`, `caisse.MvtCaisse`,
  `paiement.ModePaiement` et `cinema.Salle` : premiers modèles cinéma ;
- `arbiochem-war/web/pages/` : connexion, gabarit, gestion APJ et contrôleurs
  génériques nettoyés des dépendances Arbiochem ;
- `sql/framework-base.sql` et `sql/cinema-application-final.sql` : schémas
  technique et métier.

Les noms historiques `arbiochem-ejb` et `arbiochem-war` sont conservés dans
l'arborescence pour limiter les modifications dans le build. Ils doivent être
renommés ensemble si vous souhaitez un autre nom de projet.

## Démarrage

1. Créer et initialiser la base Oracle avec Docker (voir ci-dessous), si ce
   n'est pas déjà fait.
2. Vérifier `apj.connection.url`, `apj.connection.user` et
   `apj.connection.password` dans `arbiochem-ejb/src/java/apj.properties`.
   Le dossier `build-file/lib` doit contenir un seul pilote Oracle moderne
   compatible avec Java 8 (par exemple `ojdbc8.jar`). Les anciens pilotes
   `classes12.jar` et `ojdbc6-11.jar` sont exclus automatiquement du build.
3. Construire le WAR depuis la racine :

   ```bash
   ant -f build.xml war
   ```

   L'archive est créée dans `build-file/build/cinema.war`. Pour déployer sur
   WildFly/JBoss, définir `JBOSS_HOME`, puis lancer `ant -f build.xml deploy`.
4. Créer les modèles métier restants, les services et les écrans décrits dans
   [TODO-Cinema.md](./TODO-Cinema.md).

### Base Oracle avec Docker

Les commandes suivantes sont à lancer depuis la racine du projet. Elles
créent un conteneur Oracle Free persistant et montent le dossier `sql` en
lecture seule. Le compte applicatif `CINEMA` est créé dans `FREEPDB1`.

```bash
docker run -d \
  --name cinema-oracle \
  -p 1521:1521 \
  -e ORACLE_PASSWORD='cinema' \
  -e APP_USER=CINEMA \
  -e APP_USER_PASSWORD='cinema' \
  -v cinema-oracle-data:/opt/oracle/oradata \
  -v "$(pwd)/sql:/container-entrypoint-initdb.d:ro" \
  gvenzl/oracle-free:23.26.3
```

Attendre que la base soit prête, par exemple en consultant les journaux :

```bash
docker logs -f cinema-oracle
```

Quand le démarrage est terminé, exécuter les scripts manuellement dans le
schéma `CINEMA`, dans cet ordre (le framework doit précéder les tables
métier), puis charger les données initiales séparément :

```bash
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/framework-base.sql
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-application-final.sql
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-application-data.sql
```

`cinema-application-final.sql` crée uniquement la structure. Les insertions
initiales (référentiels de démonstration et menu) sont dans
`cinema-application-data.sql`.

Les données réelles du programme du 16 au 30 novembre 2026 sont dans un script
séparé, relançable sans dupliquer les salles, places, films ou séances :

```bash
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-donnees-reelles-2026.sql
```

Ce script ajoute les trois salles de 20 places, les tarifs et le programme de
diffusion. Il ne supprime pas les données de démonstration. Les champs
réalisateur et genre ne sont pas stockés, car ils ne sont pas encore présents
dans le schéma actuel.

Pour une base déjà initialisée, mettre à jour uniquement les rubriques Films,
Salles, Séances et Ventes (avec leurs entrées Liste et Nouveau/Nouvelle), sans
recharger ni effacer les données métier :

```bash
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-menu-sections.sql
```

Le script est relançable sans créer de doublons. Après l'avoir exécuté,
déconnecter puis reconnecter l'application pour recharger le menu gardé en
session. Les formulaires de création
film, salle et séance, ainsi que les parcours d'achat direct et les listes
correspondantes, sont désormais présents dans l'application.

Pour autoriser l'achat direct jusqu'à la fin d'une séance déjà commencée sur
une base existante, appliquer aussi la migration de disponibilité :

```bash
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-direct-sale-availability.sql
```

Le paiement valide la vente, crée la facture payée et le mouvement
d'encaissement, achète les places et émet les billets dans la même transaction.
La validation APJ de la facture reste distincte et ne retarde pas l'émission
des billets.

La suppression des codes métier (`CODE`, `CODECLIENT`) conserve les ID et les
données de chaque enregistrement. Sur une base déjà créée, appliquer la
migration suivante avant de redéployer l'application :

```bash
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-remove-business-codes.sql
```

Elle supprime ces colonnes dans `CLIENT`, `MODEPAIEMENT`, `CAISSE`,
`CINE_CATEGORIE_PLACE`, `CINE_SALLE` et `CINE_FILM`, recrée les vues de lecture
concernées et ajoute les droits APJ pour les créations de films, salles,
places et séances. Les champs de code propres au framework APJ sont conservés.
Après la migration, déconnecter puis reconnecter l'application pour recharger
les droits APJ gardés dans la session.

### Recharger toutes les données en conservant les tables

`cleanup-cinema-data.sql` vide les données cinéma et APJ métier, les menus
cinéma et les droits associés, mais conserve les tables, vues, fonctions et
séquences. Pour remettre le jeu de démonstration puis les données réelles,
arrêter d'abord l'application, puis exécuter les scripts dans cet ordre :

```bash
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cleanup-cinema-data.sql
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-application-data.sql
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-donnees-reelles-2026.sql
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-menu-sections.sql
```

Sur une base créée avec la version actuelle de `cinema-application-final.sql`,
les vues nécessaires à l'achat direct sont déjà présentes. Pour une base plus
ancienne, exécuter aussi `cinema-direct-sale-availability.sql`. Ensuite,
redémarrer ou reconnecter l'application afin qu'elle recharge le menu.

### Reconstruire tout le schéma après un changement SQL

Cette opération détruit toutes les données du schéma `CINEMA`. Arrêter
l'application, puis exécuter le script de reconstruction complet :

```bash
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/recreate-cinema-schema.sql
```

`recreate-cinema-schema.sql` enchaîne le nettoyage du schéma, la création du
framework, la création des tables cinéma et le jeu de démonstration. Pour
ajouter ensuite les données réelles du programme, exécuter :

```bash
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-donnees-reelles-2026.sql
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-menu-sections.sql
```

Si tu lances plutôt `cleanup-cinema-schema.sql` séparément, l'ordre complet
est : nettoyage du schéma, framework APJ, tables cinéma, données de base,
données réelles, puis mise à jour des menus :

```bash
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cleanup-cinema-schema.sql
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/framework-base.sql
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-application-final.sql
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-application-data.sql
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-donnees-reelles-2026.sql
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-menu-sections.sql
```

Le socle doit être rejoué après toute modification de `framework-base.sql`.

Pour faire évoluer une base dont il faut conserver les données, créer plutôt
un script de migration dédié avec les opérations `ALTER TABLE` adaptées au
changement. Le script de reconstruction sert aux réinitialisations complètes.

Vérifier que le conteneur tourne et que les scripts se sont terminés sans
erreur :

```bash
docker ps --filter name=cinema-oracle
```

Le volume `cinema-oracle-data` conserve les données après l'arrêt du
conteneur. Pour les démarrages ultérieurs, utiliser `docker start
cinema-oracle`. Les mots de passe ci-dessus sont des exemples de
développement : les remplacer par des mots de passe personnels avant toute
utilisation réelle. Le montage du dossier d'initialisation reste utile pour
les commandes `sqlplus` ci-dessus ; les scripts sont exécutés ici
manuellement afin de maîtriser leur ordre et le schéma cible.

## Conception des écrans cinéma

Le parcours écran par écran, les actions de chaque clic, la disposition des
écrans et l'arborescence JSP prévue sont décrits dans
[Ecrans-Cinema.md](./Ecrans-Cinema.md).

Les règles métier retenues pour les réservations, ventes, paiements,
validations APJ, remboursements, avoirs et mouvements de caisse sont décrites
dans [Cinema.md](./Cinema.md). Le backlog des étapes d'implémentation est dans
[TODO-Cinema.md](./TODO-Cinema.md).

Le socle de connexion, de navigation APJ et de packaging est en place. Le
parcours d'achat direct est implémenté; les parcours de réservation, de
facturation et de caisse restent à compléter.

ant -f build.xml clean war
ant -f build.xml deploy

changer heure oracle: 
docker stop cinema-oracle
docker rm cinema-oracle

docker run -d \
  --name cinema-oracle \
  -p 1521:1521 \
  -e TZ=Indian/Antananarivo \
  -e ORACLE_PASSWORD='cinema' \
  -e APP_USER=CINEMA \
  -e APP_USER_PASSWORD='cinema' \
  -v cinema-oracle-data:/opt/oracle/oradata \
  -v "$(pwd)/sql:/container-entrypoint-initdb.d:ro" \
  gvenzl/oracle-free:23.26.3
