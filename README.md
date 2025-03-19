# PetitLapin V2

Un jeu de labyrinthe où vous contrôlez un lapin qui doit collecter des "miams" tout en évitant un renard qui le poursuit.

## Règles du jeu

- Déplacez le lapin (jaune) avec les flèches directionnelles
- Collectez les miams (magenta) pour gagner 50 points
- Évitez le renard (cyan) qui vous pourchasse
- La partie se termine quand le renard attrape le lapin

## Installation

### 1. Installation de Java JDK

#### Windows

1. Téléchargez le JDK depuis [le site officiel d'Oracle](https://www.oracle.com/java/technologies/downloads/) ou optez pour une distribution OpenJDK
2. Exécutez le fichier téléchargé et suivez les instructions d'installation
3. Configurez la variable d'environnement JAVA_HOME :
   - Cliquez droit sur "Ce PC" > Propriétés > Paramètres système avancés > Variables d'environnement
   - Créez une nouvelle variable système nommée JAVA_HOME avec comme valeur le chemin d'installation du JDK (ex: C:\Program Files\Java\jdk-17)
   - Ajoutez %JAVA_HOME%\bin à la variable PATH
4. Vérifiez l'installation en ouvrant l'invite de commandes et en tapant :

```bash
java -version
```

#### macOS

1. Téléchargez le JDK depuis [le site officiel d'Oracle](https://www.oracle.com/java/technologies/downloads/) ou utilisez Homebrew :

```bash
brew install --cask temurin
```

2. Si vous avez téléchargé le package, exécutez-le et suivez les instructions

3. Vérifiez l'installation en ouvrant le Terminal et en tapant :

```bash
java -version
```

#### Linux

1. Utilisez le gestionnaire de paquets de votre distribution :

   **Debian/Ubuntu** :

```bash
sudo apt update
sudo apt install default-jdk
```

**Fedora/RHEL** :

```bash
sudo dnf install java-17-openjdk-devel
```

**Arch Linux** :

```bash
sudo pacman -S jdk-openjdk
```

2. Vérifiez l'installation :

```bash
java -version
```

### 2. Installation de Leiningen

#### Windows

1. Téléchargez le script d'installation depuis [le site officiel de Leiningen](https://leiningen.org/)
2. Placez le fichier `lein.bat` dans un dossier qui est dans votre PATH
3. Ouvrez une invite de commandes et exécutez :

```bash
lein self-install
```

#### macOS

1. Utilisez Homebrew :

```bash
brew install leiningen
```

2. Ou téléchargez le script d'installation depuis [le site de Leiningen](https://leiningen.org/) et suivez les instructions pour l'installation manuelle

#### Linux

1. Utilisez le gestionnaire de paquets de votre distribution :

   **Debian/Ubuntu** :

```bash
sudo apt update
sudo apt install leiningen
```

**Pour les autres distributions** :

```bash
mkdir -p ~/bin
curl -O https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
mv lein ~/bin/
chmod +x ~/bin/lein
```

Ajoutez ensuite `~/bin` à votre PATH si ce n'est pas déjà fait. 2. Vérifiez l'installation :

```bash
lein version
```

### 3. Installation du jeu

1. Clonez ce dépôt :

```bash
git clone https://github.com/votre-nom/my-game.git
```

2. Placez les polices nécessaires dans le dossier `resources/fonts/`

   - OurFriendElectric.otf : [Télécharger sur Dafont](https://www.dafont.com/fr/our-friend-electric.font)
   - SuiGenerisRG.otf : [Télécharger sur Dafont](https://www.dafont.com/fr/sui-generis.font)

3. Lancez l'installation :

```bash
lein install
```

## Lancement du jeu

```bash
lein run
```

## Contrôles

- Déplacement : ↑ ↓ ← →
- Rejouer après Game Over : [Espace]

## Licence

Copyright © 2025 JNCHR
Ce programme est distribué sous les termes de la licence Eclipse Public License 2.0.
