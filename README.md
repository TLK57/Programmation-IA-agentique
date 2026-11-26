# Candy Crush

Clone match-3 réalisé en Kotlin multiplateforme avec KorGE 3.4.0. Le jeu utilise le niveau fixe du cahier des charges, ses ressources graphiques/audio et une fenêtre portrait.

## Lancer le jeu et les tests

Avec JDK 17 et le wrapper Gradle fourni :

```shell
./gradlew runJvm
./gradlew jvmTest
```

Sous Windows, les commandes équivalentes sont `.\gradlew.bat runJvm` et `.\gradlew.bat jvmTest`. La fenêtre fait 540 × 960 pixels et utilise une résolution virtuelle de 810 × 1440.

## Organisation

- `j4k.candycrush.model` : `Tile`, `Row`, `GameField`, `TileCell` et `Level` définissent la grille et ses données, sans dépendance à KorGE.
- `j4k.candycrush.math.PositionGrid` : conversions entre coordonnées de cases et pixels.
- `j4k.candycrush.GameMechanics` : échanges, détection des alignements, gravité et remplissage.
- `j4k.candycrush.GameProgress` : score, multiplicateurs, objectifs, coups et résultat du niveau, testables sans fenêtre.
- `j4k.candycrush.BoardFactory` : génération injectable d'une grille aléatoire sans alignement initial.
- `j4k.candycrush.Main` : point d'entrée et présentation KorGE (saisie, affichage, animations et audio).
- `src/commonTest/kotlin` : tests déterministes du modèle, des règles, de la réserve, de la progression et de la conversion de coordonnées.

## Choix et écarts

La planche `candy_donuts.png` est le thème par défaut ; `D` active le rendu de débogage avec les lettres. Les réactions en chaîne sont résolues séquentiellement, les insertions et tirages de réserve sont déterministes dans les tests, et le hasard n'est utilisé qu'en jeu. Les raccourcis `P`, `D`, `S` et `R` sont disponibles pour le diagnostic.

Aucune dépendance externe ni modification des versions KorGE, Kotlin ou Gradle n'a été ajoutée. La configuration Gradle ne change que le point d'entrée JVM (`j4k.candycrush.MainKt`), nécessaire car le jeu respecte le paquet imposé plutôt que le paquet par défaut `MainKt` du plugin. Aucune différence intentionnelle de règles ou de niveau par rapport au cahier des charges n'est retenue.
