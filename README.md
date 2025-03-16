# PetitLapin V2

Un jeu de labyrinthe où vous contrôlez un lapin qui doit collecter des "miams" tout en évitant un renard qui le poursuit.

## Règles du jeu

- Déplacez le lapin (jaune) avec les flèches directionnelles
- Collectez les miams (magenta) pour gagner 50 points
- Évitez le renard (cyan) qui vous pourchasse
- La partie se termine quand le renard attrape le lapin

## Installation

1. Assurez-vous que Java et Leiningen sont installés sur votre système
2. Clonez ce dépôt : `git clone https://github.com/votre-nom/my-game.git`
3. Placez les polices nécessaires dans le dossier `resources/fonts/`
   - OurFriendElectric.otf : `https://www.dafont.com/fr/our-friend-electric.font`
   - SuiGenerisRG.otf : `https://www.dafont.com/fr/sui-generis.font`
4. Lancer l'installation'

```bash
        lein install
```

## Lancement du jeu

```bash
        lein run
```

## Contrôles

- Déplacement :arrow_up: :arrow_down: :arrow_left: :arrow_right:
- Rejouer apres Game Over:  [ Espace ]

## Licence

Copyright © 2025 JNCHR
Ce programme est distribué sous les termes de la licence Eclipse Public License 2.0.
