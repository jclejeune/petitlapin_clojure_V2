; src/com/jnchr/my_game/state.clj
(ns com.jnchr.my-game.state
  "État du jeu"
  (:require [com.jnchr.my-game.model :as model]))

;; État du jeu
(defonce game-state 
  (atom {:player {:x 3 :y 10}
         :enemy {:x 3 :y 0}
         :miam nil
         :last-miam-pos nil  ; Nouvelle clé pour stocker la dernière position
         :miam-alive? true
         :game-over? false
         :score 0
         :hi-score 0}))

(defn reset-game!
  "Réinitialise l'état du jeu"
  []
  (swap! game-state assoc
         :player model/initial-player-pos
         :enemy model/initial-enemy-pos
         :miam nil
         :last-miam-pos nil  ; Réinitialisation de la dernière position connue
         :miam-alive? true
         :game-over? false
         :score 0))

(defn set-player-pos! 
  "Définit la position du joueur"
  [pos]
  (swap! game-state assoc :player pos))

(defn set-enemy-pos! 
  "Définit la position de l'ennemi"
  [pos]
  (swap! game-state assoc :enemy pos))

(defn set-miam! 
  "Définit la position du miam"
  [pos]
  (swap! game-state assoc :miam pos))

(defn toggle-miam-alive! 
  "Alterne l'état d'apparition du miam"
  [alive?]
  (swap! game-state assoc :miam-alive? alive?))


(defn set-last-miam-pos! 
  "Définit la dernière position connue du miam"
  [pos]
  (swap! game-state assoc :last-miam-pos pos))

(defn game-over! 
  "Marque la partie comme terminée et met à jour le hi-score si nécessaire"
  []
  (swap! game-state (fn [state]
                      (let [new-hi-score (max (:hi-score state) (:score state))]
                        (assoc state 
                               :game-over? true
                               :hi-score new-hi-score)))))

(defn update-score! 
  "Ajoute des points au score"
  [points]
  (swap! game-state update :score + points))