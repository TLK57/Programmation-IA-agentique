# Essai de test : Codex (OpenAI) · stratégie A · Candy Crush Clone

> Essai de la **phase de test**, avant la version définitive du socle et des prompts. Il ne compte pas dans les résultats officiels, mais il donne des observations sur l'outil et des corrections à apporter au socle.

## Contexte

| | |
|---|---|
| Membre | Victor |
| Date | 25 novembre 2026, de 23 h 23 à 00 h 04 |
| Outil | Codex (OpenAI), extension pour Visual Studio Code, sous Windows |
| Modèle | GPT-6 (affiché par Codex) |
| Offre | ChatGPT Plus |
| Configuration | Mode agent, permissions par défaut. Aucune skill, aucun fichier d'instructions, aucun serveur MCP. |
| Application | Candy Crush Clone : Kotlin, KorGE 3.4.0, Gradle 7.5 |
| Point de départ | Socle commun (branche `socle`) |
| Prompts envoyés | 2 |
| Code | Branche `exp/chatgpt-A` |

## Méthode

1. Copie neuve du socle, ouverte dans VS Code.
2. Prompt **A-1**, copié tel quel depuis le document de prompts. Il demande de respecter :
   - les contraintes techniques de la section 6 du cahier des charges ;
   - l'interface imposée de la section 7.2 ;
   - les tests demandés en EQ-04 et le README demandé en EQ-07.
3. Vérification hors de l'outil : compilation et tests lancés à la main avec Gradle, sans toucher au code produit.

## Déroulé

| Tour | Ce que l'agent a livré | Ce que la vérification a trouvé |
|---|---|---|
| 1 (A-1, 7 min) | La logique du jeu (grille, échanges, alignements, chute, remplissage, objectifs, score), le niveau, des tests et un README. Il signale lui-même ce qui manque : glisser-déposer, animations, sons, musique. | Pas de compilation possible : Java 17 n'est pas installé sur le poste. |
| 2 (relance hors protocole, 32 min) | L'affichage, les images, les sons, la musique, le menu de réglages et les écrans de fin. Pour pouvoir compiler, il installe lui-même le JDK 17 et Kotlin/Native 1.7.20 en dehors du projet. | Dans l'outil, toujours pas de compilation : le bac à sable bloque le réseau, donc Gradle ne peut pas télécharger KorGE. L'agent le reconnaît honnêtement.<br>Hors de l'outil (après `gradlew --stop`), les dépendances se téléchargent, mais la compilation échoue avec **environ 60 erreurs**. |

## Mesures

| Critère | Résultat | Commentaire |
|---|---|---|
| Compilation | Échec | Environ 60 erreurs dans `LevelData.kt` et `Main.kt` |
| Contraintes techniques (section 6) | Non respectées | Versions conservées, mais le code utilise une API plus récente que KorGE 3.4.0 |
| Interface imposée (section 7.2) | Partielle | Paquet `j4k.candycrush` respecté, conformité exacte non vérifiable sans compilation |
| Tests de l'agent (EQ-04) | Écrits, non exécutés | Ne compilent pas |
| README (EQ-07) | Présent | Décrit le projet et ses limites |
| Critères d'acceptation CA-01 à CA-22 | 0 / 22 vérifiables | Le jeu ne se lance pas |
| Tests de l'auteur (36) | Non lançables | Ne compilent pas |
| Corrections manuelles | 0 | Code laissé tel quel |
| Effort humain | 2 prompts | Plus la relance de Gradle à la main pour séparer le problème réseau des erreurs de code |

## Erreurs de compilation

- **`LevelData.kt`**, lignes 14 à 17 : erreur de syntaxe dans la définition de la grille du niveau (`RESERVE` introuvable).
- **`Main.kt`** : appels à une API KorGE qui n'existe pas en 3.4.0 :
  - `resourcesVfs` et `launchImmediately` introuvables ;
  - des entiers là où la 3.4.0 attend des `Double` ;
  - `image()` et `roundRect()` ambigus ;
  - des appels `suspend` hors coroutine ;
  - `Colors.TRANSPARENT` introuvable.

## Observations

**Points forts**
- Il a lu le cahier des charges et suivi sa structure (paquet `j4k.candycrush`, logique séparée de l'affichage, tests, README).
- Il est **honnête sur ses limites** : il a dit lui-même qu'il n'avait pas pu compiler et qu'il ne fallait pas présenter la version comme validée.
- Il couvre tout le périmètre du jeu : logique, affichage, sons, menus, écrans de fin.

**Limites**
- **Sans compilation, il code à l'aveugle.** Il produit une erreur de syntaxe et des appels à une version de KorGE qui n'est pas celle imposée. C'est le risque prévu dans la note équipe (décision D2).
- **Il sort du projet sans demander** : il installe des logiciels et modifie des variables d'environnement de Windows.
- Son bac à sable bloque le réseau. Sans réglage explicite, il ne peut donc jamais compiler un projet Gradle.

## Écarts au protocole

- Le 2e prompt n'est pas la relance type A-R du document de prompts.
- L'agent a fait des installations en dehors du projet, alors que le protocole prévoit de refuser ce qui sort du projet.

## Corrections proposées pour le socle et la méthode

1. **Prérequis :** JDK 17 installé et vérifié (`java -version`) avant chaque essai.
2. **Codex :** autoriser le réseau pour Gradle. Sinon, l'agent ne peut ni compiler ni tester.
3. **Avant chaque essai :** lancer `gradlew --stop`. Un daemon Gradle démarré depuis le bac à sable garde le blocage réseau.
4. **Réponses types :** en ajouter une pour le cas où l'agent veut installer quelque chose en dehors du projet.

## Autre essai (hors protocole)

Une version web (HTML, CSS, JavaScript) a aussi été obtenue avec OpenAI. Elle s'affiche dans le navigateur, mais ne respecte pas la contrainte Kotlin / KorGE de la section 6. C'est la même observation que pour l'essai Gemini, et elle ne compte pas dans la comparaison.
