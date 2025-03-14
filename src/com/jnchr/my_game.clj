; src/com/jnchr/my_game.clj
(ns com.jnchr.my-game
  "Point d'entrée principal du jeu"
  (:require [com.jnchr.my-game.ui :as ui])
  (:gen-class))

(defn -main
  "Point d'entrée principal du jeu"
  [& args]
  (ui/create-game-window))