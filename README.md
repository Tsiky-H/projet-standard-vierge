# Projet standard vierge

Ce dossier est un socle APJ vierge, extrait de `projet-standard---Copie`.
Il ne contient aucune classe Java, page JSP ou ressource métier
Arbiochem. Les répertoires `src/java` et `web` sont volontairement
minimalistes afin d'accueillir le nouveau projet.

## Contenu conservé

- `build.xml` : compilation Ant du module EJB puis du WAR ;
- `lib/` : uniquement les bibliothèques nécessaires au socle APJ et à
  Oracle/Servlet ;
- `arbiochem-ejb/src/conf/jboss.xml` : descripteur EJB ;
- `arbiochem-war/web/WEB-INF/` : descripteurs WAR et fragment APJ ;
- `sql/framework-base.sql` : tables, séquences et données techniques du
  framework, sans tables métier.

Les noms historiques `arbiochem-ejb` et `arbiochem-war` sont conservés dans
l'arborescence pour limiter les modifications dans le build. Ils doivent être
renommés ensemble si vous souhaitez un autre nom de projet.

## Démarrage

1. Modifier les propriétés marquées `A_MODIFIER` dans
   `arbiochem-ejb/src/java/project.properties` et `apj.properties`.
2. Adapter les noms de l'application et le chemin de déploiement dans
   `build.xml`.
3. Créer les premiers modèles `ClassMAPTable` dans
   `arbiochem-ejb/src/java/<package>`.
4. Créer les JSP dans `arbiochem-war/web/pages` et les servlets dans
   `arbiochem-war/src/java`.
5. Exécuter `sql/framework-base.sql` dans le schéma Oracle cible, puis
   changer immédiatement le mot de passe du compte administrateur.
6. Lancer `ant -f build.xml compile` depuis la racine.

## Conception des écrans cinéma

Le parcours écran par écran, les actions de chaque clic, la disposition des
écrans et l'arborescence JSP prévue sont décrits dans
[Ecrans-Cinema.md](./Ecrans-Cinema.md).

Ce template ne peut pas encore afficher une application fonctionnelle : il
attend au minimum une page JSP et les classes d'authentification/menu propres
au projet à créer.
