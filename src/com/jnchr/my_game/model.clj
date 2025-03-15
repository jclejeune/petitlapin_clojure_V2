; src/com/jnchr/my_game/model.clj
(ns com.jnchr.my-game.model
  "Modèle de données du jeu")

;; Dimensions de la grille
(def cell-size 50)
(def grid-width 7)
(def grid-height 11)

;; Dimensions de la zone de jeu
(def zone-width 400)
(def zone-height 600)

;; Définition des murs (1 = mur, 0 = vide)
(def walls
  [[0 0 0 0 0 0 0]
   [0 1 0 1 0 1 0]
   [0 0 0 1 0 0 0]
   [0 1 0 0 0 1 0]
   [0 0 0 1 0 0 0]
   [0 1 0 0 0 1 0]
   [0 0 0 1 0 0 0]
   [0 1 0 0 0 1 0]
   [0 0 0 1 0 0 0]
   [0 1 0 1 0 1 0]
   [0 0 0 0 0 0 0]])

;; Délai de réapparition du miam (en millisecondes)
(def miam-respawn-delay-min 1600)
(def miam-respawn-delay-max 2400)

;; Délai de déplacement de l'ennemi (en millisecondes)
(def enemy-move-delay 300)

;; Couleurs
(def color-background [50 50 50])
(def color-wall [221 46 68])
(def color-overlay [100 100 100 150])
(def color-player :yellow)
(def color-enemy :cyan)
(def color-miam :magenta)
(def color-score-display [173 216 230])

;; Positions initiales
(def initial-player-pos {:x 3 :y 10})
(def initial-enemy-pos {:x 3 :y 0})

;; Score et hi-score
(def initial-score 0)
(def initial-hi-score 0)