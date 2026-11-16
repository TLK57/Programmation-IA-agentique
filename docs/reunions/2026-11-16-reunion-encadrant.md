# Réunion avec l'encadrant · 16 novembre 2026

Visio d'environ 20 minutes avec M. Tabbone. Présents : Abdullah, Salmane, Victor.

## Ce qu'on a présenté

- L'organisation : dépôt GitHub, protocole d'expérimentation, grille d'évaluation, plan du rapport, planning jusqu'à mars.
- La répartition des outils : Abdullah sur Copilot et JetBrains, Salmane sur Claude Code, Victor sur Gemini. Chaque essai est vérifié par un autre membre.
- La méthode : une application codée à la main, refaite par chaque agent à partir d'un projet vide et du même cahier des charges, avec trois stratégies (A : tout en un prompt, B : prompts progressifs, C : corrections et itérations).
- L'essai d'échauffement avec Claude Code (todo-list Flask, 4 prompts, environ 2 h) : l'agent annonçait 98 % de couverture, mais la vérification a trouvé un crash et des failles de sécurité.
- La problématique de départ : les agents IA rendent-ils le développement plus facile, ou déplacent-ils la difficulté vers la vérification et la maintenance ?

## Ce qui est validé

- L'application de référence n'a pas besoin d'être un projet universitaire. Projet perso, d'alternance ou donné par un prof : peu importe, tant qu'elle a été **codée à la main, sans IA générative**.
- On peut prendre **plusieurs applications** (par exemple l'application web et l'application mobile). Le choix se fera par un vote dans l'équipe.
- Ajouter ChatGPT / Codex aux outils testés ne pose pas de problème.

## Ce qu'il demande

- **Avant d'écrire les prompts**, relire ensemble les applications de référence, les corriger et les mettre à jour, pour que tout le monde sache ce qu'il y a dedans. Sans ça, on ne peut pas comparer.
- **Fixer les critères d'évaluation avant de lancer les essais** : une fois les prompts écrits, tout va très vite (une application réécrite en environ 2 h).
- Lui envoyer un mail quand on aura des choses à montrer.

## Sur la problématique

Pour lui, « un agent code 10 fois plus vite qu'un humain » est acquis. C'est le point de départ, pas la conclusion. Ce qui l'intéresse :

- **La maintenance** : le développement représente moins de 30 % du coût d'un logiciel. Si l'outil fait gagner 80 % sur le développement mais fait perdre 30 % en maintenance, le logiciel coûte plus cher au total.
- **Le cahier des charges** : tous les agents ne le rédigent pas aussi bien (selon lui, ChatGPT fait mieux que Claude sur ce point).
- **Les tests** : un agent peut tester ce qu'un autre agent a produit, et se tester lui-même, plus ou moins bien.
- **L'évolution** : est-ce facile d'ajouter une fonctionnalité au code généré ?
- **Les compétences** que le développeur perd ou garde.

## Ce qu'on en retient

- Reformuler la problématique autour du **coût global** du logiciel, pas seulement de la vitesse de développement.
- Ajouter à la grille un **test de maintenance** (nouvelle fonctionnalité, correction de bug : temps passé et régressions) et des **tests croisés entre agents**.
- Envisager de faire rédiger le cahier des charges par ChatGPT.

## Actions

- [ ] Voter pour la ou les applications de référence
- [ ] Relire ensemble les applications choisies, les corriger et les documenter
- [ ] Reformuler la problématique et la valider en équipe
- [ ] Compléter et figer la grille d'évaluation (maintenance, tests croisés, cahier des charges)
- [ ] Rédiger le cahier des charges
- [ ] Décider si on ajoute ChatGPT / Codex (abonnement de Victor)
- [ ] Envoyer un mail à l'encadrant dès qu'il y a quelque chose à montrer
