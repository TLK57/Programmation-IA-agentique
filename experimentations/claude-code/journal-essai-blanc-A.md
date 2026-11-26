# Journal d'essai : Claude Code · stratégie A · ESSAI À BLANC

> Essai à blanc (rodage), hors résultats officiels. Sert à tester le socle, les prompts et la procédure de mesure.
> Ce fichier est volontairement HORS du worktree `essai-claude-code-A`, pour que l'outil ne le voie pas.

## Contexte

| | |
|---|---|
| Membre | Abdullah |
| Date(s) | 25 novembre 2026 |
| Outil et version | Claude Code v2.1.296, lancé dans le terminal (PowerShell, `claude.cmd`) |
| Modèle utilisé | Opus 5.5 (niveau de réflexion : réglage par défaut, à confirmer) |
| Offre / abonnement | Claude Pro |
| Mode de permissions | manual (choisi à la main : le défaut de la v2.1.296 est « auto ») |
| Configuration | pas de skills, pas de plug-ins, pas de MCP, pas de CLAUDE.md |
| Environnement | Windows, JDK 17 (`C:\Users\abdul\.jdks\ms-17.0.21.1` via `JAVA_HOME`) |
| Branche / dossier | `exp/claude-code-A`, worktree `essai-claude-code-A` (créé depuis `socle`, commit `d964785`) |
| Prompt | A-1 copié tel quel |
| Session Claude Code | `39a96eaa-53ad-4acb-9695-81e3d8f97ed5` (reprise possible : `claude --resume 39a96eaa-53ad-4acb-9695-81e3d8f97ed5`) |

## Vérifications avant lancement (20 h 43)

- `git status` : sur `exp/claude-code-A`, arbre propre.
- `git config user.name` : TLK57.
- Dossier : pas de `build/`, `.gradle/`, `deps/`, ni CLAUDE.md.
- `JAVA_HOME` = `C:\Users\abdul\.jdks\ms-17.0.21.1`.

## Déroulé

| Heure | Prompt ou action | Réaction de l'agent | Intervention humaine ? |
|---|---|---|---|
| 20 h 48 | Lancement `claude.cmd`, connexion compte Claude (abonnement) | Notice : « Auto mode is now Claude Code's default permission mode » (+ 1 notice masquée) | Passage manuel en mode « manual » (Maj+Tab) |
| ≈ 21 h 00 | Prompt A-1 collé tel quel (heure exacte à confirmer par Abdullah) | Commence par explorer la config Gradle, la version de Java et le cache Gradle | |
| 21 h 01 | | Lit les figures du cahier des charges (ex. `docs/figures/ecran-4-victoire.png`) | |
| 21 h 01-02 | | Cherche les jars KorGE 3.4.0 (jvm et sources) dans le cache Gradle et inspecte l'API réelle avec `javap` (classe `Point` imposée par la section 7.2), au lieu de l'inventer | |
| 21 h 04-06 | | Annonce : « Je lis le cahier des charges et explore l'API KorGE 3.4.0 dans le cache Gradle avant d'écrire le code. » Compteur affiché : 4 fichiers lus, 4 commandes shell. Inspecte le glisser-déposer, le son et les ninepatch | |
| 21 h 08 | | Commence à écrire le code : `Tile.kt` (enum des bonbons, `getTile`, `randomTile`, `candies`). Commentaires KDoc en anglais | Passage en mode « accept edits » |
| 21 h 08-19 | | Écrit les fichiers (création automatique, non détaillée) | |
| 21 h 19 | | Première compilation : `./gradlew compileKotlinJvm` via un shell bash (il s'adapte à Windows avec `./gradlew`) | |
| 21 h 22-23 | | Erreurs de compilation : conflits de noms avec l'API KorGE dans le bouton (`onClick`, `isOver`, `isPressed`). Corrigées par l'outil avec sed/perl, puis recompilation | |
| ≈ 21 h 28 | | Lance le jeu (`runJvm`) en arrière-plan | |
| 21 h 28-34 | | Annonce « Le jeu se lance ; je capture la fenêtre pour vérifier l'affichage. » Se fabrique un script PowerShell de capture d'écran pour vérifier l'interface lui-même ; premier essai raté (fenêtre introuvable par son titre), corrigé en cherchant par le processus java | |
| 21 h 39 | | « Now the end screen texts » : corrige l'écran de fin d'après sa propre capture (boutons plus larges, texte plus petit). A ajouté dans le code du jeu un mode capture caché, déclenché par la variable d'environnement `CANDY_SHOT` (écrit « SHOTS DONE » dans le log) | |
| 21 h 39-22 h 00 | | (détail non relevé : autorisations de lecture / corrections acceptées sans capture) | |
| 22 h 00 | | Lance les tests unitaires : `./gradlew jvmTest` | |
| 22 h 03 | | Script Python qui compte tests / échecs dans `build/test-results/jvmTest/*.xml` | |
| 22 h 14 | | `./gradlew clean jvmTest` (recompilation complète + tous les tests) | |
| ≈ 22 h 15-21 | | Annonce 66 tests réussis, corrige `GameSessionTest`, ajoute `ContractTest.kt` (appelle l'interface 7.2 avec des arguments nommés), retire « le code de capture temporaire de GameScene », supprime des membres jugés inutilisés (`Row.toList`, `Tile.candies`), écrit le README (116 lignes), relance le jeu, ferme ses instances | |
| 22 h 21 | | Fin : « Worked for 22m 7s · done 22:21 ». Résumé final (voir plus bas). Message « Auto-update failed: claude.exe in use » | |
| | | | |

## Autorisations demandées par l'outil

| Heure | Commande / fichier | Réponse |
|---|---|---|
| 21 h 01 | Bash « Show Gradle config and Java version » : `cat` des fichiers Gradle et `.gitignore`, `java -version`, `echo $JAVA_HOME`, liste du cache Gradle `~/.gradle/caches/...` filtrée sur korge. Lecture seule. L'outil propose « Yes, and switch to auto mode » | 1. Yes (sans passer en auto) |
| 21 h 01 | Bash « Locate KorGE artifacts in Gradle cache » : `ls` dans `~/.gradle/caches/modules-2/files-2.1` (korge2, korma). Lecture seule | 2. Yes + autorisation permanente de lire `~/.gradle/caches/modules-2/files-2.1` pour ce projet |
| 21 h 02 | Bash « Inspect korma Point class signature » : `javap` (JDK 17) sur `korma-jvm-3.4.0.jar`, classe `com.soywiz.korma.geom.Point` et son Companion. Lecture seule | 1. Yes |
| 21 h 04 | Bash « Inspect KorGE input, audio and ninepatch APIs » : `javap` sur korge/korau/korim 3.4.0 (`MouseDragInfo`, `onMouseDrag`, `SoundChannel`, `Sound`) et `unzip -l` (ninepatch, Text, Image). Lecture seule | 1. Yes |
| 21 h 06 | Bash « Find drag input and ninepatch view classes » : `unzip -l` et `javap` sur `korge-jvm-3.4.0.jar` (`MouseDragInfo`, NinePatch, KeysEvents, Text). Lecture seule | 1. Yes |
| 21 h 08 | Création de `Tile.kt` (premier fichier de code) | 2. Yes + passage en mode « accept edits » pour la session : création et modification de fichiers sans demander, les commandes (gradlew, git…) demandent toujours |
| 21 h 19 | Bash « Compile JVM sources » : `./gradlew compileKotlinJvm --console=plain` (filtré sur erreurs, avertissements, BUILD/FAIL) | 2. Yes + ne plus demander pour `./gradlew compileKotlinJvm --console=plain` |
| 21 h 22 | Bash « Rename button callback and recompile » : `cd … && sed -i / perl -pi` sur `view/ImageButton.kt` et `view/SettingsMenu.kt` (paramètre `onClick` renommé `action`), puis recompilation. Redemandé malgré « accept edits » (commande composée avec `cd` + écriture) | 1. Yes (refus de « don't ask again for: cd * », trop large) |
| 21 h 23 | Bash « Rename button state vars and recompile » : `perl -pi` sur `view/ImageButton.kt` (`isOver` → `hovered`, `isPressed` → `pressedDown`), puis recompilation | 1. Yes |
| 21 h 28 | PowerShell : création d'un script de capture d'écran `shot.ps1` (appels `user32.dll` : FindWindow, SetForegroundWindow, GetWindowRect, CopyFromScreen) dans le dossier temporaire de Claude Code (`AppData\Local\Temp\claude\…\scratchpad`, hors du dépôt). Alerte « Command contains script block that may execute arbitrary code » | 1. Yes |
| 21 h 34 | PowerShell « Locate game window by process and screenshot » : corrige `shot.ps1` (recherche de la fenêtre par le processus java/javaw) et le lance pour capturer le jeu dans `scratchpad\s1.png`. Alerte « Command name is a dynamic expression » | 1. Yes |
| 21 h 39 | Bash « Rerun game with internal screenshots » : `perl -pi` sur `view/EndScreen.kt` (BUTTON_WIDTH 280 → 300, BUTTON_TEXT_SIZE 44 → 38), `rm -f "$SP"/*.png` dans le scratchpad (alerte « Dangerous rm operation on possibly-empty variable path » : $SP défini juste avant, sans risque), puis `CANDY_SHOT="$SP" ./gradlew runJvm` en arrière-plan et attente de « SHOTS DONE » dans `build/run.log` | 1. Yes |
| 22 h 00 | Bash « Run JVM unit tests » : `./gradlew jvmTest --console=plain` | 2. Yes + ne plus demander pour `./gradlew jvmTest --console=plain` |
| 22 h 03 | Bash « Summarize test results » : script `python -` qui lit les rapports XML de tests | 1. Yes (refus de « don't ask again for: python - », trop large) |
| 22 h 14 | Bash « Clean build and run all tests » : `./gradlew clean jvmTest` + comptage `awk` | 1. Yes |
| | | |

## Réponses types utilisées (section 8 des prompts)

| Heure | Question de l'outil | Réponse type (ligne n°) |
|---|---|---|
| | | |

## Bilan chiffré

| Mesure | Valeur |
|---|---|
| Heure de début → fin | ≈ 21 h 00 → 22 h 21 |
| Temps total | ≈ 1 h 21 de bout en bout. L'outil affiche « Worked for 22m 7s » (sens exact à vérifier : temps de calcul ou temps du dernier tour ?) |
| Nombre de prompts (A-1 + relances A-R) | 1 (A-1 seul, aucune relance) |
| Autorisations accordées / refusées | |
| Réponses types | 0 (l'outil n'a posé aucune question) |
| Corrections manuelles (sans IA) | 0 |
| Ce que l'agent annonce (tests écrits, jvmTest, runJvm) | `jvmTest` réussi : 67 tests, 0 échec, aucun avertissement de compilation. `runJvm` ouvre la fenêtre « Candy Crush » sans erreur. Rien commité. N'a pas joué à la main |
| Limite d'usage Claude Pro atteinte ? | Non |
| Commit | `exp(claude-code-A): génération initiale`. `git status` à 22 h 26 : `build.gradle.kts` modifié ; nouveaux : `README.md`, `src/commonMain/kotlin/j4k/`, `src/commonTest/kotlin/j4k/` ; `deps/` exclu du commit |

## Résumé final de l'outil (22 h 21, reformulé)

- Vérification : n'a pas joué à la main. A fait jouer des scénarios automatiques avec captures internes au jeu (code temporaire, dit retiré). D'après lui : grille de départ conforme, 42 coups, A = 18, C = 22, score 0 ; premier coup = 240 points (6 × 20 × 2) avec « x2 » ; réglages, écran « Bravo ! » et images de lettres (touche D) corrects. Son, toucher et glisser à la souris non testés en vrai.
- Incident : sa première capture Windows a mis le jeu au premier plan mais a photographié l'écran de l'utilisateur (une vidéo en plein écran). Il dit avoir supprimé l'image aussitôt, puis est passé aux captures internes au jeu.
- Fait : logique de la section 7.2 (model, math, GameMechanics) + paquet `game` (coup entier : vagues, score, multiplicateur, objectifs, victoire / défaite, rejouer, mélanger), sans KorGE sauf `Point`. Vue : une scène, animations, glisser souris / toucher, score et objectifs, réglages, écran de fin, sons, musique, touches de débogage P, D, S, R. Tests : toute la liste EQ-04 + test « contrat » de l'interface 7.2, sans hasard (source injectée ou graine fixe). README.
- Choix annoncés : la logique calcule tout le coup et renvoie la liste des vagues, la vue les rejoue ; `Level(grille, réserve, maxMoves = 42, objectives = aucun, randomTiles)`, réserve de type `GameField?` ; `isSwapAllowed` n'applique que EF-03 (voisines et dans la grille) ; première vague ×2 si l'échange forme au moins deux alignements distincts ; son de suppression puis son de combo selon le multiplicateur, `drop_ground.mp3` non utilisé ; chute 0,5 s par ligne (jeu « un peu lent ») ; « Mélanger » et « Nouvelle grille » rechargent aussi la réserve ; cases de 123 px, grille à y = 250.
- Écarts annoncés : modification de `build.gradle.kts` (ajout de `jvmMainClassName = "j4k.candycrush.MainKt"`), car sinon KorGE impose un point d'entrée hors du paquet `j4k.candycrush`, en contradiction avec ET-05 ; versions et dépendances inchangées, justifié dans le README. Alignement horizontal des textes ignoré par KorGE 3.4 avec les polices bitmap : refait à la main (`TextAlign.kt`). « Aucune exigence non respectée, à ma connaissance. »
- Signale que `deps/` (non suivi par git) est créé par la compilation (plugin kproject).

## Remarques pour le rodage (à corriger avant de figer)

- Socle / cahier des charges : ET-05 (point d'entrée dans `j4k.candycrush`) entre en conflit avec la config KorGE du socle. L'outil a dû modifier `build.gradle.kts`. Corriger le socle (ajouter `jvmMainClassName`) ou ET-05 avant de figer, sinon chaque outil butera dessus ou modifiera la config.
- `.gitignore` du socle : n'exclut pas `deps/` (créé par kproject à la compilation). L'ajouter au socle ; pour ce commit, ne pas ajouter `deps/`.
- Confidentialité : la capture d'écran de l'outil a photographié l'écran de l'utilisateur. Règle à ajouter pour les essais : rien de personnel ouvert à l'écran pendant un essai.

- Claude Code v2.1.296 : le mode par défaut est devenu « auto ». La règle d'or dit à la fois « configuration par défaut » et « mode qui demande » : à trancher en équipe, et à appliquer pareil aux 5 outils.
- Le prompt A-1 écrit `./gradlew` (syntaxe Linux/Mac). Sous Windows, Claude Code passe par un shell bash et lance `./gradlew` sans problème. Pour les autres outils, à vérifier.
- Lancement dans l'onglet Code de l'application : par défaut environnement Cloud + dépôt sur `main` (qui contient `2-equipe-seulement/`) + mode Auto. Piège à signaler dans le document de prompts (tableau 2.4).
- Le worktree partage le `.git` du dépôt : l'outil pourrait lire `main` (prompts, note équipe) avec git. À surveiller.

## Observations

Ce qui a bien marché :

- Vérification visuelle de sa propre initiative : l'outil se construit un script de capture d'écran (PowerShell + user32.dll) pour regarder la fenêtre du jeu, sans que le prompt le demande.
- Exploration avant de coder : config, figures, puis vérification de l'API réelle de KorGE 3.4.0 dans le cache Gradle (`javap`). Bonne pratique contre les hallucinations d'API.

Problèmes rencontrés (hallucinations, erreurs, boucles…) :

- Malgré l'exploration de l'API au début, conflits de noms avec KorGE découverts seulement à la compilation (bouton : `onClick`, `isOver`, `isPressed`).
- À chaque demande d'autorisation, l'outil pousse vers le mode auto (« Tip: auto mode handles these prompts for you », option « Yes, and switch to auto mode »). En mode manual, beaucoup d'autorisations pour de simples lectures : coût en temps d'intervention humaine à mesurer.

Décisions prises par nous et pas par l'agent :

- 21 h 08 : passage du mode manual au mode « accept edits » (la réponse type aux demandes de modification de fichiers est toujours « Oui », donc même résultat, moins de clics). Mode à fixer en équipe pour les essais officiels, pareil pour les 5 outils.

À vérifier à la revue de code :

- Mode capture caché `CANDY_SHOT` : l'outil dit l'avoir retiré de `GameScene`. À vérifier (grep `CANDY_SHOT`, `SHOTS DONE`).
- Suppression de `Tile.candies` et `Row.toList` : font-elles partie de l'interface imposée 7.2 ? Si oui, les tests de l'auteur ne compileront pas.
- Touches de débogage P, D, S, R : demandées par le cahier des charges ?
- `isSwapAllowed` limité à EF-03 et règle du ×2 dès la première vague : conformes au cahier des charges et aux tests de l'auteur ?
- Modification de `build.gradle.kts` : à garder dans le commit de l'IA tel quel, et à discuter dans le rapport.
- Séparation logique / affichage : paquet `j4k.candycrush.view` pour l'affichage.

Questions pour le cahier des charges :

- Faut-il autoriser ou interdire les outils annexes que l'agent se fabrique (ici un script de capture d'écran hors du dépôt) ? À uniformiser entre les outils. Risque : la capture prend le rectangle de la fenêtre à l'écran, donc une autre fenêtre placée par-dessus peut être capturée.

- Le code est commenté en anglais. Le cahier des charges impose-t-il une langue pour le code et les commentaires ?
