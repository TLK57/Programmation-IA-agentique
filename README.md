# Candy Crush Clone (match 3) – KorGE 3.4.0

Jeu de type « match 3 » réalisé en Kotlin avec KorGE 3.4.0, selon le cahier des charges
`docs/Cahier-des-charges-candy-crush-clone.md`.

## Lancer le jeu et les tests

Prérequis : JDK 17 (`JAVA_HOME` doit pointer dessus). Le wrapper Gradle 7.5 est fourni.

```bash
./gradlew runJvm     # lance le jeu (fenêtre « Candy Crush », 540 × 960, portrait)
./gradlew jvmTest    # lance les tests unitaires
```

Sous Windows : `gradlew.bat runJvm` et `gradlew.bat jvmTest`.

### Commandes

- Jouer : appuyer sur un bonbon et le glisser vers une case voisine (souris ou toucher).
- Roue dentée (en haut à droite) : ouvre / ferme le menu (sons, musique, mélanger, recommencer).
- Touches de débogage : `P` affiche la grille en notation texte dans la console, `D` bascule
  vers les images de lettres A–E, `S` mélange, `R` recommence.

## Organisation du code

Tout le code est sous le paquet `j4k.candycrush` (`src/commonMain/kotlin/j4k/candycrush`).
La logique ne dépend d'aucune classe d'affichage, de son ou d'entrée de KorGE (seul `Point`
de `com.soywiz.korma.geom` est utilisé, par `PositionGrid`) ; la présentation dépend de la
logique, jamais l'inverse.

### Logique (testable sans fenêtre)

| Paquet / classe | Rôle |
|---|---|
| `model.Tile` | Contenu d'une case : bonbons `A`–`E`, `Wall`, `Hole`, `OutOfSpace` ; conversion lettre ↔ valeur, tirage au hasard. |
| `model.Row` | Une ligne de la grille ; lecture hors ligne = `OutOfSpace`. |
| `model.GameField` | La grille : accès par colonne/ligne ou par position, lignes, colonnes, copie, notation texte (`toString` / `fromString`). |
| `model.TileCell` | Un bonbon et sa position. |
| `model.Level` | Un niveau : grille, réserve, nombre de coups, objectifs ; `getNextTile` applique la règle de la réserve (EF-23). |
| `math.PositionGrid` | Correspondance cases ↔ pixels. |
| `GameMechanics` | Actions élémentaires sur une grille : échange, validité, alignements, suppression, chute, remplissage. |
| `game.GameSession` | Une partie : joue un coup complet (échange, vagues de réactions en chaîne, score, multiplicateur, objectifs, coups, victoire / défaite), recommencer, mélanger. Renvoie un `MoveResult` qui décrit chaque vague (`Wave`) pour que la présentation l'anime. |
| `game.ScoreRules` | Règles de score et de multiplicateur (20 points par bonbon, ×1 / ×2, +1 par vague). |
| `game.DefaultLevel` | Les données du niveau imposé (grille 5 × 9, réserve, 42 coups, 18 A et 22 C). |
| `game.FieldGenerator` | Génère une grille au hasard sans alignement (mélanger / nouvelle grille). |

### Présentation (`j4k.candycrush.view`) et point d'entrée

| Classe | Rôle |
|---|---|
| `main.kt` (`j4k.candycrush.MainKt`) | Ouvre la fenêtre (résolution virtuelle 810 × 1440) et affiche la scène. |
| `GameScene` | La scène unique : relie gestes, logique, animations, sons et interface ; bloque les gestes pendant les animations et l'écran de fin. |
| `BoardView` | Affiche les bonbons et joue les animations (échange, aller-retour, suppression, chute, arrivée des nouveaux bonbons). |
| `SwipeInput` | Transforme un glisser (souris ou toucher) en demande d'échange (EF-01, EF-02). |
| `Hud` | Coups restants, objectifs, score, « x2 »…, points flottants. |
| `SettingsMenu`, `ImageButton` | Roue dentée et colonne de boutons avec états survol / clic / activé. |
| `EndScreen` | Fenêtre « Bravo ! » / « Perdu ! » avec « Rejouer » et « Nouvelle grille ». |
| `AudioPlayer` | Effets sonores et musique (activation / coupure). |
| `Assets` | Chargement des images, polices et sons ; découpe des planches de bonbons. |
| `TextAlign.kt` | Alignement horizontal des textes en police bitmap. |

### Tests (`src/commonTest/kotlin`)

- `model/ModelTest.kt` : `Tile`, `Row`, `GameField` (notation texte, accès, hors grille, copie, colonnes), `Level` (réserve).
- `GameMechanicsTest.kt` : échange, validité, alignements (3, plus de 3, horizontal, vertical, croisement, trous), chute, déplacements, remplissage et son ordre.
- `math/PositionGridTest.kt` : correspondance cases ↔ pixels.
- `game/GameSessionTest.kt` : score et multiplicateur (vague simple, croisement ×2, réaction en chaîne), objectifs, victoire (y compris au dernier coup), défaite, recommencer, mélanger, génération sans alignement.
- `ContractTest.kt` : appel de l'interface imposée (section 7.2) avec des arguments nommés.

Les tests ne dépendent ni de l'affichage ni du hasard : la source de bonbons est injectée
(`tileStore`, paramètre `randomTiles` de `Level` et `GameSession`, ou `Random` avec graine fixe).

## Choix faits là où le cahier des charges laisse la liberté

- **Architecture** : la logique résout un coup entier d'un seul tenant (`GameSession.play`) et
  renvoie la liste des vagues (bonbons supprimés, déplacements, insertions, score cumulé,
  objectifs) ; la vue rejoue ensuite ces vagues avec des animations. Pas de bus d'événements.
- **`Level`** : paramètres supplémentaires `maxMoves` (défaut 42), `objectives` (défaut : aucun)
  et `randomTiles` (source de bonbons quand la réserve est vide, défaut : hasard uniforme).
  La réserve est un `GameField?`. Une colonne de réserve absente ou vide donne un bonbon au hasard.
- **`isSwapAllowed`** suit EF-03 seulement ; le contrôle « cases voisines et dans la grille »
  (EF-02) est fait par `GameSession.play`, qui ignore alors le geste.
- **`getNewTileMoves`** : à ligne égale, les trous sont pris de gauche à droite.
- **`PositionGrid.getField`** fait la division entière demandée ; une méthode
  `isPixelOnGrid` en plus écarte les pixels à gauche / au-dessus de la grille.
- **Multiplicateur de la première vague** : nombre d'alignements distincts (≥ 3) passant par
  les deux cases échangées ; ×2 dès qu'il y en a au moins deux.
- **Points flottants** : un seul texte par vague, placé sur le bonbon du milieu de la liste
  des bonbons supprimés (triée par ligne puis colonne).
- **Sons** : le son de suppression est joué à la première vague d'un coup ; les vagues de
  réaction en chaîne jouent le son de combo correspondant au multiplicateur (`multi_2` à
  `multi_6`, ce dernier pour ×6 et au-delà). `drop_ground.mp3` n'est pas utilisé.
- **Animations** : échange et suppression 0,2 s ; chute 0,5 s par ligne parcourue ; les
  bonbons existants et les nouveaux (apparition en fondu depuis le haut de la colonne)
  tombent en même temps. La grille est découpée (clip) pour que les nouveaux bonbons
  apparaissent depuis son bord supérieur.
- **Mélanger / Nouvelle grille** : la grille est remplie au hasard sans alignement ; la
  réserve est rechargée depuis les données du niveau, comme pour « Rejouer ».
- **Taille des cases** : la plus grande taille qui tient dans 810 px de large (marges de
  90 px) et entre y = 250 et 80 px du bas, soit 123 px ; grille centrée horizontalement.
- **Gestes** : la distance « une taille de case » est la distance euclidienne depuis le
  point de départ ; la case d'arrivée est celle sous le pointeur à ce moment-là.
- **Fin de partie** : l'écran de fin assombrit légèrement tout l'écran ; tant qu'il est
  affiché, la grille et le menu de réglages ne réagissent plus (seuls ses deux boutons agissent).

## Écarts et remarques

- **`build.gradle.kts`** : ajout d'une seule ligne, `jvmMainClassName = "j4k.candycrush.MainKt"`.
  Par défaut le plugin KorGE cherche `MainKt` dans le paquet racine, ce qui obligerait à placer
  le point d'entrée hors de `j4k.candycrush` (contraire à ET-05). Les versions de KorGE, Kotlin
  et Gradle ne sont pas modifiées. Aucune dépendance ajoutée.
- Les polices bitmap n'appliquent pas l'alignement horizontal dans cette version de KorGE :
  les textes sont alignés à la main à partir de leurs bornes (`TextAlign.kt`).
- La fenêtre s'ouvre en 540 × 960 ; sur un écran moins haut, KorGE/le système peut la réduire
  (le rendu reste à l'échelle, en portrait).
- Seule la cible JVM a été vérifiée (cibles web / Android non évaluées).
