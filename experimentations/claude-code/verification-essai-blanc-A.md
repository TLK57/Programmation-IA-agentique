# Vérification de l'essai à blanc : Claude Code · stratégie A

| | |
|---|---|
| Version vérifiée | branche `exp/claude-code-A`, commit `771215a` |
| Tests de l'auteur | dépôt TobseF, branche `master`, commit `842c751` |
| Date | 26 novembre 2026 |
| Qui | Abdullah (mesure Gradle et parties jouées) ; tests automatiques, mutations et revue de code préparés avec l'aide de Claude, dans une conversation séparée de l'essai |
| Fichiers | `verification-essai-blanc-A/` : script de mesure, résultat, scripts du pré-contrôle |

## 1. Pourquoi cette vérification

Pendant l'essai à blanc du 25 novembre, l'agent a annoncé que tout marchait : 67 tests réussis, jeu qui se lance. Mais un agent qui se note lui-même, c'est un peu comme un élève qui corrige sa propre copie : il ne trouvera pas les erreurs auxquelles il n'a pas pensé. C'est pourquoi nous avons tout revérifié sans nous appuyer sur son compte rendu, de trois façons :

- relancer les tests sur une copie propre du code, en ajoutant les 36 tests de l'auteur que l'agent n'a jamais vus ;
- mesurer la qualité de ses tests avec des mutations (une mutation, c'est une petite erreur que l'on glisse exprès dans le code pour voir si les tests la remarquent) ;
- jouer au jeu, scénario par scénario, avec les critères CA-06 à CA-18 du cahier des charges.

## 2. Bilan des 22 critères d'acceptation

| Critère | Résultat | Détail |
|---|---|---|
| CA-01 | oui | `gradlew clean jvmTest` réussi depuis un clone propre, sous Windows avec le JDK 17 |
| CA-02 | oui | la fenêtre « Candy Crush » s'ouvre en portrait |
| CA-03 | oui | versions inchangées ; une seule ligne ajoutée dans `build.gradle.kts`, justifiée dans le README |
| CA-04 | oui | les 36 tests de l'auteur compilent sans aucune modification |
| CA-05 | 36 / 36 | 100 % des tests de l'auteur réussis |
| CA-06 | oui | grille de départ identique ligne par ligne, 42 coups, objectifs 18 et 22, score 0 |
| CA-07 | oui | aller-retour, son d'erreur, aucun coup retiré |
| CA-08 | oui, avec une remarque | diagonale et sortie de la grille ignorées ; le cas « 2 cases » est presque impossible à provoquer (voir section 6) |
| CA-09 | oui | un coup valide retire un coup et donne 60 points |
| CA-10 | oui | les deux premiers bonbons de la colonne viennent bien de la réserve, puis du hasard |
| CA-11 | oui | « x2 », « x3 » et points affichés ; un seul coup a rapporté 1 660 points |
| CA-12 | oui | des gestes rapides pendant les animations ne cassent rien |
| CA-13 | oui | « Bravo ! » s'affiche une fois la chaîne terminée (victoire avec 22 coups restants) |
| CA-14 | non vérifiable à la main | impossible de perdre, même en jouant mal exprès ; vérifié par les tests de l'agent |
| CA-15 | oui | « Recommencer » remet la grille, la réserve et les compteurs |
| CA-16 | oui | « Mélanger » donne une grille sans alignement et remet les compteurs |
| CA-17 | non | couper la musique fige le jeu (voir section 3) |
| CA-18 | non testé | pas d'écran tactile |
| CA-19 | oui | la logique du jeu ne dépend pas de l'affichage |
| CA-20 | en partie | 67 / 67 tests, sans hasard ; 20 mutations sur 20 détectées ; couverture pas encore mesurée |
| CA-21 | oui | README complet, en français |
| CA-22 | non mesuré | outil d'analyse du code pas encore choisi |

En clair : sur les 13 scénarios joués à la main, 10 sont bons, 1 échoue, 1 ne peut pas être vérifié en jouant et 1 n'a pas pu être testé.

## 3. Le bug de la musique (CA-17)

Ce que nous avons vu : activer la musique fonctionne, mais dès qu'on la coupe, le jeu se fige et tout le PC ralentit. Le problème revient à chaque fois (deux essais sur deux). Pour en sortir, il faut faire Ctrl + C dans le terminal.

La cause probable se trouve dans `view/AudioPlayer.kt`. La musique tourne dans une boucle sans fin qui enchaîne les morceaux, en attendant la fin de chacun. Pour bien comprendre, imaginons un lecteur de CD réglé pour passer au morceau suivant dès que le précédent se termine. Si on coupe le son, chaque morceau se « termine » aussitôt : le lecteur en relance un autre, puis un autre, des milliers de fois par seconde. Or, en Kotlin, l'ordre d'arrêter cette boucle n'est lu que pendant une pause, et la boucle n'en fait plus jamais. Elle tourne donc sans fin et occupe tout le jeu. Ce n'est qu'une hypothèse tirée de la lecture du code, mais elle colle avec ce que nous avons observé.

Pourquoi l'agent ne l'a pas vu : il l'a dit lui-même, il n'a jamais testé le son. Il vérifiait son travail avec des captures d'écran, et une capture d'écran n'a pas d'oreilles. De plus, aucun test automatique ne couvre le son.

Ce que ça change pour le projet :

- nous ne corrigeons pas ce bug dans cette version, car le code de l'IA doit rester tel quel ;
- c'est un bon candidat pour le premier prompt de la stratégie C (corrections et itérations) ;
- pour le rapport, c'est un exemple parfait : 100 % aux tests de l'auteur, 67 tests au vert, et pourtant un bug bloquant que seul un humain qui joue a trouvé. Nous avons vu la même chose lors de l'échauffement du 9 novembre : l'agent ne trouve pas ce qu'il ne pense pas à tester.

## 4. La qualité des tests : les mutations

Nous avons glissé 20 erreurs dans la logique du jeu, une par une (par exemple : compter 10 points par bonbon au lieu de 20, ou faire gagner la défaite avant la victoire), puis lancé les deux séries de tests. Une erreur est « détectée » si au moins un test échoue.

| Série de tests | Erreurs détectées |
|---|---|
| Tests de l'agent (67) | 20 sur 20 |
| Tests de l'auteur (36) | 10 sur 20 |

Les tests de l'agent sont donc solides sur toute la logique. Il ne faut pas oublier un détail : l'une des erreurs (la réserve qui ne descend plus) n'a été attrapée que parce que les tests tournaient à l'infini. Sous Gradle, la commande resterait bloquée au lieu d'afficher un échec. Il faudra prévoir un temps maximal pour les tests.

Les tests de l'auteur, eux, ne regardent ni le score ni la fin de partie. Par conséquent, ils ne suffisent pas à comparer les outils : une version pourrait obtenir 100 % avec un score complètement faux.

Le détail des 20 mutations est dans `verification-essai-blanc-A/pre-controle/mut.py`. Ces mutations ont été faites à la main : pour le rapport, il faudra choisir un vrai outil de mutation.

## 5. Revue du code

Points vérifiés à partir de la liste du journal :

| Point | Constat |
|---|---|
| Suppression de `Tile.candies` et `Row.toList` | sans conséquence, ni l'interface imposée ni les tests de l'auteur ne les utilisent |
| Mode capture caché (`CANDY_SHOT`) | bien retiré, comme l'agent l'avait annoncé |
| Touches P, D, S, R | prévues par le cahier des charges (EF-39, optionnelle) |
| Règle de l'échange valide (`isSwapAllowed`) | conforme, et la grille reste intacte après le contrôle |
| Multiplicateur ×2 dès la première vague | conforme à EF-18 |
| Séparation logique / affichage | conforme : la logique n'importe rien de l'affichage, du son ou des entrées |

Autres remarques, qui ne sont pas des écarts au cahier des charges :

- `Level(grille, réserve)` sans autre paramètre crée un niveau sans objectif, donc gagné dès le premier coup. Le risque est clair pour le test de maintenance M-1 (ajouter un deuxième niveau) : un outil qui oublie les objectifs fera un niveau gagné d'avance.
- La logique calcule tout le coup d'un seul bloc, puis l'affichage le rejoue en animation. C'est propre, mais à garder en tête pour le test M-2.
- Le code et les commentaires sont en anglais, le README en français.
- Sur 3 000 parties jouées au hasard par un programme, 10 finissent sur une grille bloquée (plus aucun coup possible). Le cahier des charges met ce cas hors périmètre, et le joueur peut toujours mélanger.

## 6. Ce qu'il faut corriger avant les essais officiels

- Socle : le fichier `gradlew` n'est pas marqué exécutable dans git (comme chez l'auteur). Sous Linux ou macOS, `./gradlew` échoue depuis un clone propre. À corriger avec `git update-index --chmod=+x gradlew`.
- CA-08 : le cas « glisser sur 2 cases » ne peut presque pas se produire, car le jeu déclenche l'échange dès que le doigt a parcouru une case (EF-01). Il faut le retirer ou préciser « geste très rapide ».
- CA-14 : le niveau est trop facile, impossible de perdre en jouant (et aucune défaite sur 3 000 parties automatiques). Si nous ne changeons rien, aucun outil ne pourra être vérifié sur ce point. Notre proposition : accepter la vérification par les tests pour CA-14, et exiger dans EQ-04 un test de défaite facile à retrouver.
- CA-17 : écrire noir sur blanc qu'il faut activer puis couper les sons et la musique, puisque c'est exactement là que le bug est apparu.
- CA-18 : savoir si un membre de l'équipe a un écran tactile ; sinon, prévoir « non testé » pour tous les outils.
- Grille d'évaluation : prévoir un temps maximal pour les tests, choisir les outils de couverture et d'analyse du code, et décider comment compter un critère « non vérifiable » ou « non testé ».

## 7. Comment refaire la mesure

Le script `verification-essai-blanc-A/lancer-jvmTest-officiel.ps1` fait toute la mesure des tests en une commande : il clone la branche dans un dossier temporaire, ajoute les 36 tests de l'auteur dans `src/commonTest/kotlin/reference/`, lance `gradlew clean jvmTest` et compte les résultats. Il est prévu pour le dossier du projet sur le PC d'Abdullah (`depot-github` et clone de l'auteur). Pour un autre outil, il suffit de changer le nom de la branche et les chemins en haut du script. Nous pourrons donc l'utiliser pour les cinq outils, ce qui garantit que tout le monde mesure de la même façon.
