; src/com/jnchr/my_game/state.clj
(ns com.jnchr.my-game.state
  "État du jeu"
  (:require [com.jnchr.my-game.model :as model]))

;; État du jeu
(defonce game-state 
  (atom {:player {:x 3 :y 10}
         :enemy {:x 3 :y 0}
         :miam nil
         :miam-alive? true
         :game-over? false
         :score 0
         :hi-score 0}))

;; Fonctions de mise à jour de l'état

(defn reset-game! 
  "Réinitialise l'état du jeu"
  []
  (swap! game-state assoc
         :player model/initial-player-pos
         :enemy model/initial-enemy-pos
         :miam nil
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