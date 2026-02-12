L3 CMPL - Projet Compilateur
============================

## VS Code

Dépendance et extensions VSCode :
- Extension Pack for Java  v0.30.5 
- ANTLR4 grammar syntax support - Mike Lischke - v2.4.7

## Architecture du projet et description des fichiers

- `src/` : les fichiers sources Java à consulter, utiliser et/ou compléter
   - `analyseurs/` : paquet contenant les analyseurs lexical et syntaxique
   - `compilateur/` : paquet contenant tout le nécessaire à l'implémentation des points de génération
   - `edl/` : paquet contenant tout le nécessaire à l'implémentation de l'éditions de liens
   - `exec/` : paquet pour l'exécution de la machine MAPILE fournie
- `lib/` : les bibliothèques Java requises
   - `antlr-3.5.2-complete.jar` : ANTLR3, pour générer le code Java des analyseurs lexical et syntaxique à partir d'un fichier de grammaire `.g`
   - `libIO.jar` : fonctionnalités d'entrées/sorties
   - `libMapile.jar` : la machine exécutable MAPILE
- `testsFournis/` : fichiers en langage source PROJET et cible MAPILE.
   Attention : ce ne sont que des exemples et ils ne permettent pas de tester l'ensemble des possibilités.
   En particulier, pour tester les cas d'erreur, on ne fournit que des fichiers testant les déclarations et expressions.
   À vous de compléter l'ensemble des programmes de tests, pour vous assurer du bon fonctionnement du compilateur.
   - `*.pro` : fichiers sources PROJET
      - `DeclExp-T1.pro` : teste les déclarations et les expressions
      - `DeclExp-Err*.pro` : testent certaines erreurs dans les déclarations, un type d'erreur par fichier
      - `polyPxx-exoyy.pro` : exercice n˚yy, page xx du poly
      - `polyPxx-exempleyy.pro` : exemple page xx du poly
	   - `TDexoxx.pro` : exercice n˚xx distribué en TD
   - `*.genENS` : fichiers compilés de référence, en format mnémoniques
   - `*.objENS` : fichiers compilés de référence, en format objet
   - `*.descENS` : fichiers de descripteurs de référence (compilation séparée)
   NB: les extensions `.gen`, `.obj` et `.desc` des fichiers compilés ont été suffixées par `ENS` pour que votre compilateur n'écrase pas ces fichiers de référence.
- `bin/` : répertoire contenant les fichiers Java compilés `.class`
- `.vscode` : configuration de VS Code et des extensions VS Code

## Compiler le projet, et exécuter les programmes

Le processus complet est :
1. générer les fichiers Java des analyseurs lexical et syntaxique 
2. compiler tous les fichiers source Java du projet
3. exécuter votre compilateur sur un programme source PROJET
4. exécuter votre éditeur de liens, si besoin
5. exécuter sur la machine MAPILE le programme produit par vos compilateur/éditeur de liens  

### Générer les fichiers Java des analyseurs avec ANTLR3

À partir d'un fichier ANTLR de grammaire `fichier.g`, pour générer les analyseurs lexical et syntaxique, exécutez la commande suivante dans un terminal, en adaptant les chemins d'accès et le nom de fichier de grammaire.

```bash
java -jar chemin/vers/jar/antlr-3.5.2-complete.jar chemin/vers/fichier.g
```

Si la génération réussit, trois fichiers (préfixés par le nom `fichier` sans le `.g`) seront produits :
- `fichierLexer.java` : l'analyseur lexical, à côté du fichier de la grammaire
- `fichierParser.java` : l'analyseur syntaxique, à côté du fichier de la grammaire
- `fichier.tokens` : fichier auxiliaire, dans le répertoire courant, vous pouvez l'ignorer

La commande précédente doit être ré-exécutée après chaque modification du fichier `fichier.g`, pour que les analyseurs lexical et syntaxique soient re-générés, et tiennent compte des modifications.

### Compiler les fichiers Java du projet

La compilation des fichiers Java peut être entièrement réalisée par VS Code. Assurez-vous que l'extension VS Code Java s'est déclenchée. La compilation Java est réalisée automatiquement à chaque sauvegarde de fichiers modifiés.

Pour compiler tous les fichiers Java du projet en ligne de commande, exécuter la commande suivante, en adaptant si besoin les chemins d'accès et les noms de fichiers.

```bash
javac -d bin/ -cp "lib/*" src/*/*.java
```

### Exécuter les programmes principaux

Les trois programmes principaux sont le compilateur, l'éditeur de liens (compilation séparée), et la machine d'exécution MAPILE.

Une fois compilés, chacun peut être exécuté directement depuis VS Code, en cliquant droit sur le nom du fichier :
- `src/compilateur/Compilateur.java` : le compilateur
- `src/edl/Edl.java` : l'éditeur de liens
- `src/exec/Mapile.java` : la machine d'exécution MAPILE

Ils peuvent également être exécutés depuis la ligne de commande, en exécutant une commande de la forme suivante, en adaptant les noms du paquet du fichier principal, et éventuellement en ajoutant un/des noms de fichiers d'entrée à passer en argument.

```
java -cp bin/:lib/* nomcomplet.paquet.ProgrammePrincipal sibesoin/chemin/vers/fichierTest.extension
```
