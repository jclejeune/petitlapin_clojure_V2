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

;; (defn spawn-miam
;;   "Génère une position aléatoire pour le miam"
;;   []
;;   (let [empty-positions (get-empty-positions)
;;         new-pos (when (seq empty-positions) (rand-nth empty-positions))]
;;     (when new-pos
;;       (state/set-miam! new-pos))))

(defn spawn-miam
  "Génère une position aléatoire pour le miam à une distance minimale de 3 cases du joueur
   et différente de la dernière position"
  []
  (let [state @state/game-state
        player (:player state)
        last-miam-pos (:last-miam-pos state)  ; Récupère la dernière position du miam
        ;; Récupère toutes les positions vides
        all-empty-positions (get-empty-positions)
        ;; Filtre pour ne garder que les positions assez éloignées du joueur
        distant-positions (filter
                           (fn [{x :x y :y}]
                             (let [px (:x player)
                                   py (:y player)
                                    ;; Distance de Manhattan (plus appropriée pour un mouvement en grille)
                                   manhattan-dist (+ (Math/abs (- x px)) (Math/abs (- y py)))]
                               (>= manhattan-dist 3)))
                           all-empty-positions)
        ;; Filtre pour exclure la dernière position du miam (si elle existe)
        filtered-positions (if last-miam-pos
                             (filter #(not= % last-miam-pos) distant-positions)
                             distant-positions)
        ;; Si aucune position ne respecte les contraintes, on revient aux positions distantes,
        ;; puis à toutes les positions vides si nécessaire
        usable-positions (cond
                           (seq filtered-positions) filtered-positions
                           (seq distant-positions) distant-positions
                           :else all-empty-positions)
        ;; Sélectionne une position aléatoire parmi celles disponibles
        new-pos (when (seq usable-positions) (rand-nth usable-positions))]

    ;; Place le miam à la nouvelle position s'il y en a une
    (when new-pos
      ;; Sauvegarde la nouvelle position comme dernière position connue
      (state/set-last-miam-pos! new-pos)
      (state/set-miam! new-pos))))

;; (defn move-enemy
;;   "Déplace l'ennemi vers le joueur avec un comportement plus naturel"
;;   []
;;   (let [state @state/game-state]
;;     (when-not (:game-over? state)
;;       (let [{px :x py :y} (:player state)
;;             {ex :x ey :y} (:enemy state)
;;             moves [[-1 0] [1 0] [0 -1] [0 1]]
;;             valid-moves (filter #(can-move? (+ ex (first %)) (+ ey (second %))) moves)

;;             ;; Calcul des distances pour chaque mouvement possible
;;             moves-with-distances (map (fn [[dx dy]]
;;                                         {:move [dx dy]
;;                                          :distance (distance (+ ex dx) (+ ey dy) px py)})
;;                                       valid-moves)

;;             ;; Trouver la distance minimum
;;             min-distance (if (seq moves-with-distances)
;;                            (apply min (map :distance moves-with-distances))
;;                            Double/MAX_VALUE)

;;             ;; Filtrer les mouvements qui donnent cette distance minimum
;;             best-moves (filter #(= (:distance %) min-distance) moves-with-distances)

;;             ;; S'il y a plusieurs meilleurs mouvements possibles (équidistance),
;;             ;; choisir un mouvement au hasard parmi ceux-ci
;;             selected-move (if (seq best-moves)
;;                             (:move (rand-nth best-moves))
;;                             nil)]

;;         (when selected-move
;;           (state/set-enemy-pos! {:x (+ ex (first selected-move))
;;                                  :y (+ ey (second selected-move))}))))))

(defn move-enemy
  "Déplace l'ennemi vers le joueur avec un comportement adaptatif"
  []
  (let [state @state/game-state]
    (when-not (:game-over? state)
      (let [{px :x py :y} (:player state)
            {ex :x ey :y} (:enemy state)
            moves [[-1 0] [1 0] [0 -1] [0 1]]
            valid-moves (filter #(can-move? (+ ex (first %)) (+ ey (second %))) moves)

            ;; Calcul des distances pour chaque mouvement possible
            moves-with-distances (map (fn [[dx dy]]
                                        {:move [dx dy]
                                         :distance (distance (+ ex dx) (+ ey dy) px py)})
                                      valid-moves)

            ;; Trouver la distance minimum
            min-distance (if (seq moves-with-distances)
                           (apply min (map :distance moves-with-distances))
                           Double/MAX_VALUE)

            ;; Filtrer les mouvements qui donnent cette distance minimum
            best-moves (filter #(= (:distance %) min-distance) moves-with-distances)

            ;; Historique des 5 derniers déplacements
            last-positions (:last-enemy-positions state)
            new-history (take 5 (conj last-positions {:x ex :y ey}))

            ;; Vérifier si une position revient trop souvent
            position-counts (frequencies new-history)
            stuck-zone? (some #(>= (val %) 3) position-counts) ;; Si une position est apparue 3 fois en 5 tours

            ;; Déterminer le mode de chasse
            hunting-mode? (and (not stuck-zone?) (= (count best-moves) 1))

            ;; Sélection du mouvement
            selected-move (if stuck-zone?
                            ;; Si bloqué dans une zone, mouvement aléatoire
                            (rand-nth valid-moves)
                            ;; Sinon, suivre la stratégie habituelle
                            (:move (first best-moves)))]

        ;; Mise à jour de l'historique
        (swap! state/game-state assoc :last-enemy-positions new-history)

        ;; Mise à jour du mode hunting
        (state/set-hunting-mode! hunting-mode?)

        ;; Déplacement de l'ennemi
        (when selected-move
          (state/set-enemy-pos! {:x (+ ex (first selected-move))
                                 :y (+ ey (second selected-move))}))))))



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
      ;; On sauvegarde d'abord la position actuelle comme dernière position connue
      (state/set-last-miam-pos! miam)
      ;; Puis on réinitialise la position actuelle
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
