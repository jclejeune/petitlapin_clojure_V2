; src/com/jnchr/my_game/core.clj
(ns com.jnchr.my-game.core
  "Logique du jeu"
  (:require [com.jnchr.my-game.model :as model]
            [com.jnchr.my-game.state :as state]))

;; Fonctions de logique du jeu

(defn can-move?
  "Vérifie si un déplacement est possible à la position donnée"
  [x y]
  (and (>= x 0) (< x model/grid-width)
       (>= y 0) (< y model/grid-height)
       (= 0 (get-in model/walls [y x]))))

(defn distance
  "Calcule la distance euclidienne entre deux points"
  [x1 y1 x2 y2]
  (Math/sqrt (+ (Math/pow (- x1 x2) 2) (Math/pow (- y1 y2) 2))))

(defn move-player
  "Déplace le joueur si possible"
  [dx dy]
  (let [state @state/game-state]
    (when-not (:game-over? state)
      (let [{:keys [x y]} (:player state)
            new-x (+ x dx)
            new-y (+ y dy)]
        (when (can-move? new-x new-y)
          (state/set-player-pos! {:x new-x :y new-y})
          true)))))

(defn get-empty-positions
  "Trouve toutes les positions vides sur la grille"
  []
  (let [{:keys [player enemy]} @state/game-state]
    (for [y (range model/grid-height)
          x (range model/grid-width)
          :when (and (= 0 (get-in model/walls [y x]))
                     (not= {:x x :y y} player)
                     (not= {:x x :y y} enemy))]
      {:x x :y y})))

(defn spawn-miam
  "Génère une position aléatoire pour le miam"
  []
  (let [empty-positions (get-empty-positions)
        new-pos (when (seq empty-positions) (rand-nth empty-positions))]
    (when new-pos
      (state/set-miam! new-pos))))

(defn move-enemy
  "Déplace l'ennemi vers le joueur en utilisant la stratégie du plus court chemin"
  []
  (let [state @state/game-state]
    (when-not (:game-over? state)
      (let [{px :x py :y} (:player state)
            {ex :x ey :y} (:enemy state)
            moves [[-1 0] [1 0] [0 -1] [0 1]]
            valid-moves (filter #(can-move? (+ ex (first %)) (+ ey (second %))) moves)
            best-move (apply min-key (fn [[dx dy]]
                                       (distance (+ ex dx) (+ ey dy) px py))
                             valid-moves)]
        (when best-move
          (state/set-enemy-pos! {:x (+ ex (first best-move))
                                 :y (+ ey (second best-move))}))))))

(defn check-game-over
  "Vérifie si le joueur a été attrapé par l'ennemi"
  []
  (let [{:keys [player enemy]} @state/game-state]
    (when (= player enemy)
      (state/game-over!))))

(defn check-for-miam
  "Vérifie si le joueur a attrapé un miam"
  []
  (let [{:keys [player miam miam-alive?]} @state/game-state]
    (when (and miam-alive? miam (= player miam))
      (state/update-score! 50)
      (state/set-miam! nil)
      (state/toggle-miam-alive! false)
      true)))

(defn respawn-miam-after-delay
  "Fait réapparaître un miam après un délai aléatoire"
  []
  (future
    (Thread/sleep (+ model/miam-respawn-delay-min 
                     (rand-int (- model/miam-respawn-delay-max 
                                  model/miam-respawn-delay-min))))
    (spawn-miam)
    (state/toggle-miam-alive! true)))

(defn handle-key-press
  "Gère les événements clavier"
  [key-code repaint-fn]
  (condp = key-code
    java.awt.event.KeyEvent/VK_SPACE 
    (when (:game-over? @state/game-state)
      (state/reset-game!)
      (repaint-fn)
      true)
    
    java.awt.event.KeyEvent/VK_LEFT  (move-player -1 0)
    java.awt.event.KeyEvent/VK_RIGHT (move-player 1 0)
    java.awt.event.KeyEvent/VK_UP    (move-player 0 -1)
    java.awt.event.KeyEvent/VK_DOWN  (move-player 0 1)
    
    nil))

(defn game-loop
  "Boucle principale du jeu"
  [repaint-fn]
  (let [action-listener (proxy [javax.swing.AbstractAction] []
                          (actionPerformed [e]
                            (move-enemy)
                            (check-game-over)
                            (when (check-for-miam)
                              (respawn-miam-after-delay))
                            (repaint-fn)))]
    (doto (javax.swing.Timer. model/enemy-move-delay action-listener)
      (.start))))
