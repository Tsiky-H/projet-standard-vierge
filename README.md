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
métier) :

```bash
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/framework-base.sql
docker exec -it cinema-oracle sqlplus -L CINEMA/cinema@//localhost:1521/FREEPDB1 @/container-entrypoint-initdb.d/cinema-application-final.sql
```

Vérifier que le conteneur tourne et que les deux scripts sont terminés sans
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

Le socle de connexion, de navigation APJ et de packaging est en place. Les
écrans de réservation, de vente, de facturation et de caisse restent à
implémenter.
