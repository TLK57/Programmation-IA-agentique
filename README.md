# Candy Crush Clone

## Lancer le jeu et les tests

Le projet utilise JDK 17 et le wrapper Gradle 7.5 fourni.

```powershell
gradlew.bat runJvm
gradlew.bat jvmTest
```

Sur macOS ou Linux, utiliser `./gradlew runJvm` et `./gradlew jvmTest`.

## Organisation

- `j4k.candycrush.model` contient `Tile`, `Row`, `GameField` et `Level`, ainsi que la notation texte des grilles.
- `j4k.candycrush.math.PositionGrid` convertit les positions de cases en positions d'ecran.
- `GameMechanics` gere les echanges, alignements, chutes et remplissages. `GameState` gere les points, objectifs, coups et fin de niveau.
- `Main.kt` construit l'interface KorGE et gere les commandes. `LevelData` contient la grille et la reserve imposees.
- `GameContractTest` couvre les regles principales sans dependance a l'affichage ni au hasard pour les remplissages testes.

Le niveau reprend la grille 5 x 9, la reserve 5 x 2, les 42 coups et les objectifs A = 18 et C = 22 du cahier des charges. Les bonbons utilisent la planche d'images fournie. Les commandes sont disponibles a la souris et au toucher; la partie peut aussi etre jouee en selectionnant successivement deux cases voisines.

Les echanges invalides sont annules avec un son d'erreur. Les echanges valides resolvent les alignements, les reactions en chaine, la chute, le remplissage, les points et les objectifs. Les sons et la musique se reglent depuis le menu. Le menu permet de rejouer ou de generer une grille sans alignements au depart.

KorGE 3.4.0 et le wrapper Gradle fourni sont conserves. Aucune dependance externe n'a ete ajoutee. Les transitions sont presentees par etapes courtes; elles ne comportent pas encore toutes les interpolations visuelles suggerees par les captures.

## Validation locale

JDK Temurin 17 a ete installe pour l'utilisateur Windows courant. Rouvrir VS Code ou ouvrir un nouveau terminal pour prendre en compte `JAVA_HOME` et `PATH`.

Dans cet environnement, les commandes `jvmTest` et `runJvm` ont ete lancees, mais Gradle n'a pas pu telecharger les dependances Maven requises pour `compileKotlinJvm` (par exemple Kotlin Serialization et les bibliotheques KorGE). Les connexions de Gradle aux depots Maven Central et Google echouent avec `Permission denied: getsockopt`. Il faut relancer les deux commandes dans un environnement ou Gradle peut telecharger les dependances. Leur resultat reste a confirmer.

Les scripts Windows `run-game.ps1` et `test-game.ps1` configurent le JDK et le compilateur Kotlin/Native 1.7.20 installes dans le profil utilisateur avant d'appeler Gradle. Ils sont utiles si l'environnement PowerShell autorise les scripts locaux.
