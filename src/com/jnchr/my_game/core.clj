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
  "Génère une position aléatoire pour le miam avec des contraintes simples"
  []
  (let [state @state/game-state
        player (:player state)
        last-miam-pos (:last-miam-pos state)

        ;; Récupère toutes les positions vides
        all-empty-positions (get-empty-positions)

        ;; Filtre pour positions assez éloignées du joueur (distance de Manhattan)
        player-distant-positions (filter
                                  (fn [{x :x y :y}]
                                    (let [px (:x player)
                                          py (:y player)
                                          manhattan-dist (+ (Math/abs (- x px)) (Math/abs (- y py)))]
                                      (>= manhattan-dist 3)))
                                  all-empty-positions)

        ;; Exclure la dernière position du miam
        filtered-positions (if last-miam-pos
                             (filter #(not= % last-miam-pos) player-distant-positions)
                             player-distant-positions)

        ;; Si aucune position ne respecte les contraintes, on revient aux positions simples
        usable-positions (cond
                           (seq filtered-positions) filtered-positions
                           (seq player-distant-positions) player-distant-positions
                           :else all-empty-positions)

        ;; Sélectionner une position aléatoire parmi les utilisables
        selected-position (when (seq usable-positions)
                            (rand-nth usable-positions))]

    ;; Place le miam à la nouvelle position s'il y en a une
    (when selected-position
      (state/set-last-miam-pos! selected-position)
      (state/set-miam! selected-position))))


(defn respawn-miam-after-delay
  "Fait réapparaître un miam après un délai aléatoire"
  []
  (future
    (Thread/sleep (+ model/miam-respawn-delay-min
                     (rand-int (- model/miam-respawn-delay-max
                                  model/miam-respawn-delay-min))))
    (spawn-miam)
    (state/toggle-miam-alive! true)))

(defn move-player
  "Déplace le joueur si possible et vérifie immédiatement la collision avec un miam"
  [dx dy]
  (let [state @state/game-state]
    (when-not (:game-over? state)
      (let [{:keys [x y]} (:player state)
            new-x (+ x dx)
            new-y (+ y dy)]
        (when (can-move? new-x new-y)
          (state/set-player-pos! {:x new-x :y new-y})
          ;; Vérifier immédiatement la collision avec le miam
          (when (check-for-miam)
            (respawn-miam-after-delay))
          true)))))



(defn move-enemy
  "Déplace l'ennemi vers le joueur avec un comportement adaptatif avancé"
  []
  (let [state @state/game-state]
    (when-not (:game-over? state)
      (let [{px :x py :y} (:player state)
            {ex :x ey :y} (:enemy state)
            moves [[-1 0] [1 0] [0 -1] [0 1]]
            valid-moves (filter #(can-move? (+ ex (first %)) (+ ey (second %))) moves)

            ;; Historique des positions
            last-positions (:last-enemy-positions state)
            new-history (take 10 (conj (or last-positions []) {:x ex :y ey})) ;; Augmente la taille de l'historique à 10

            ;; Détection d'un cycle ou d'une zone bloquée
            position-counts (frequencies new-history)
            stuck? (some #(>= (val %) 3) position-counts) ;; Si une position est apparue 3+ fois

            ;; Calcul du chemin quand bloqué
            escape-strategy (if stuck?
                              ;; Si bloqué, utiliser une stratégie d'exploration
                              (let [;; Détermine les positions déjà visitées récemment
                                    recent-positions (set (map #(select-keys % [:x :y]) (take 5 new-history)))
                                    ;; Évalue chaque mouvement possible
                                    moves-with-scores (map
                                                       (fn [[dx dy]]
                                                         (let [new-pos {:x (+ ex dx) :y (+ ey dy)}
                                                               ;; Pénalise les positions récemment visitées
                                                               position-score (if (contains? recent-positions new-pos) 10 0)
                                                               ;; Score basé sur la distance de Manhattan vers le joueur
                                                               manhattan-dist (+ (Math/abs (- (+ ex dx) px))
                                                                                 (Math/abs (- (+ ey dy) py)))
                                                               ;; Score final (plus petit = meilleur)
                                                               score (+ manhattan-dist position-score)]
                                                           {:move [dx dy] :score score}))
                                                       valid-moves)
                                    ;; Trie par score (préférence pour nouveaux chemins)
                                    sorted-moves (sort-by :score moves-with-scores)]
                                (when (seq sorted-moves)
                                  (:move (first sorted-moves))))
                              ;; Si pas bloqué, stratégie normale de poursuite
                              (let [moves-with-distances (map (fn [[dx dy]]
                                                                {:move [dx dy]
                                                                 :distance (distance (+ ex dx) (+ ey dy) px py)})
                                                              valid-moves)
                                    min-distance (if (seq moves-with-distances)
                                                   (apply min (map :distance moves-with-distances))
                                                   Double/MAX_VALUE)
                                    best-moves (filter #(= (:distance %) min-distance) moves-with-distances)]
                                (when (seq best-moves)
                                  (:move (rand-nth best-moves)))))

            ;; Détermination du mouvement final
            selected-move (or escape-strategy
                              ;; Si pas de mouvement sélectionné et des mouvements valides existent, choisir au hasard
                              (when (seq valid-moves) (rand-nth valid-moves)))]

        ;; Mise à jour de l'historique
        (swap! state/game-state assoc :last-enemy-positions new-history)

        ;; Mise à jour du mode hunting (pour les visuels)
        (state/set-hunting-mode! (and (not stuck?) (= (count valid-moves) 1)))

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


(defn handle-key-press
  "Gère les événements clavier"
  [key-code repaint-fn]
  (condp = key-code
    java.awt.event.KeyEvent/VK_SPACE
    (when (:game-over? @state/game-state)
      (state/reset-game!)
      (repaint-fn)
      true)

    java.awt.event.KeyEvent/VK_LEFT
    (let [moved? (move-player -1 0)]
      (when moved? (repaint-fn))
      moved?)

    java.awt.event.KeyEvent/VK_RIGHT
    (let [moved? (move-player 1 0)]
      (when moved? (repaint-fn))
      moved?)

    java.awt.event.KeyEvent/VK_UP
    (let [moved? (move-player 0 -1)]
      (when moved? (repaint-fn))
      moved?)

    java.awt.event.KeyEvent/VK_DOWN
    (let [moved? (move-player 0 1)]
      (when moved? (repaint-fn))
      moved?)

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
