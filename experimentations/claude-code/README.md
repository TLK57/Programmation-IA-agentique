# claude-code

Installation, configuration et journaux d'essais pour cet outil. C'est Abdullah qui fait les essais Claude Code.

- Version testée : Claude Code v2.1.296 (essai à blanc du 25 novembre 2026)
- Modèle utilisé : Opus 5.5 (niveau de réflexion par défaut)
- Offre (gratuite / étudiante / payante) : payante, abonnement Claude Pro, connexion par le compte (pas de clé d'API)

## Configuration pour les essais

| Réglage | Valeur |
|---|---|
| Lancement | dans le terminal PowerShell, avec `claude.cmd`, depuis le dossier de l'essai, après avoir réglé `JAVA_HOME` sur le JDK 17 |
| Skills, plug-ins, MCP | aucun |
| Fichier d'instructions (CLAUDE.md) | aucun |
| Mode de permissions | manual au départ, puis accept edits (création de fichiers sans demander, commandes toujours demandées) ; à fixer en équipe pour les essais officiels |

Il ne faut pas oublier un piège de cette version : le mode de permissions par défaut est devenu « auto ». L'outil agit alors sans rien demander, ce qui fausse le comptage des autorisations. Il faut donc changer de mode avec Maj + Tab dès le lancement.

Pourquoi le terminal plutôt que l'onglet Code de l'application Claude : l'onglet Code ne voit pas le `JAVA_HOME` du terminal, et il démarre par défaut dans le cloud, sur la branche `main` du dépôt. Or `main` contient les documents réservés à l'équipe (`cahier-des-charges/2-equipe-seulement/`), que l'outil ne doit jamais lire.

## Essais

| Date | Essai | Statut | Fichiers |
|---|---|---|---|
| 9 novembre 2026 | Échauffement : todo-list Flask | hors protocole | `echauffement-todo.md` |
| 25 novembre 2026 | Candy Crush Clone, stratégie A | essai à blanc (rodage), hors résultats | `journal-essai-blanc-A.md`, code sur la branche `exp/claude-code-A` |
| 26 novembre 2026 | Vérification de l'essai à blanc | faite | `verification-essai-blanc-A.md`, scripts dans `verification-essai-blanc-A/` |

En clair, pour l'essai à blanc : 36 tests de l'auteur sur 36 réussis, 67 tests de l'agent sur 67, mais un bug bloquant trouvé en jouant (couper la musique fige le jeu). L'essai officiel sera refait quand le socle, le cahier des charges et les prompts seront figés.
