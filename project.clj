; Structure du projet
; my-game
; ├── project.clj
; ├── src
; │   └── com
; │       └── jnchr
; │           ├── my_game
; │           │   ├── core.clj       ; Logique du jeu
; │           │   ├── model.clj      ; Modèle de données
; │           │   ├── state.clj      ; État du jeu
; │           │   └── ui.clj         ; Interface utilisateur
; │           └── my_game.clj        ; Point d'entrée
; └── test
;     └── com
;         └── jnchr
;             └── my_game
;                 └── core_test.clj  ; Tests unitaires

; project.clj
(defproject com.jnchr/my-game "0.1.0-SNAPSHOT"
  :description "Jeu de labyrinthe avec lapin et renard"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :main ^:skip-aot com.jnchr.my-game
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
