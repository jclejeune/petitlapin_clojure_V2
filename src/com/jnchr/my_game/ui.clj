(ns com.jnchr.my-game.ui
  "User Interface"
  (:import [java.awt Color Font]
           [javax.swing JFrame JPanel]
           [java.awt.event KeyListener])

  (:require [com.jnchr.my-game.model :as model]
            [com.jnchr.my-game.state :as state]
            [com.jnchr.my-game.core :as core]))

;; Font loading function
(defn load-font
  "Loads a font from the resources/fonts directory."
  [filename size]
  (let [font-path (str "resources/fonts/" filename)
        font-stream (java.io.FileInputStream. font-path)
        base-font (Font/createFont Font/TRUETYPE_FONT font-stream)]
    (.deriveFont base-font (float size))))

;; Load specific fonts
(def game-over-font (load-font "OurFriendElectric.otf" 54))
(def default-font (load-font "SuiGenerisRG.otf" 25))

(defn draw-grid
  "Dessine la grille et les murs avec des couleurs spécifiques."
  [g]
  (let [[r g-color b] model/color-background
        margin-x (/ (- model/zone-width (* model/grid-width model/cell-size)) 2)
        margin-y (/ (- model/zone-height (* model/grid-height model/cell-size)) 2)]

    ;; Dessine le fond de la grille
    (.setColor g Color/BLACK)
    (.fillRect g 0 0 model/zone-width model/zone-height)
    (.setColor g (Color. r g-color b))
    (.fillRect g margin-x margin-y
               (* model/grid-width model/cell-size)
               (* model/grid-height model/cell-size))

    ;; Dessine les murs avec la couleur basée sur la valeur du mur
    (doseq [y (range model/grid-height)
            x (range model/grid-width)]
      (let [value (get-in model/walls [y x])]
        (when (> value 0) ;; Si la case contient un mur (valeur > 0)
          (let [color (get model/colors-wall value [0 0 0])] ;; Récupère la couleur du mur ou noir si non trouvé
            (.setColor g (Color. (first color) (second color) (last color))) ;; Applique la couleur correspondante
            (.fillRect g
                       (+ margin-x (* x model/cell-size))
                       (+ margin-y (* y model/cell-size))
                       model/cell-size model/cell-size)))))

    ;; Dessine les lignes de la grille
    (.setColor g Color/BLACK)
    (dotimes [i (inc model/grid-width)]
      (.drawLine g
                 (+ margin-x (* i model/cell-size))
                 margin-y
                 (+ margin-x (* i model/cell-size))
                 (+ margin-y (* model/grid-height model/cell-size))))
    (dotimes [j (inc model/grid-height)]
      (.drawLine g
                 margin-x
                 (+ margin-y (* j model/cell-size))
                 (+ margin-x (* model/grid-width model/cell-size))
                 (+ margin-y (* j model/cell-size))))))

(defn draw-entity
  "Draws an entity at the given position with the specified color."
  [g pos color]
  (let [{:keys [x y]} pos
        margin-x (/ (- model/zone-width (* model/grid-width model/cell-size)) 2)
        margin-y (/ (- model/zone-height (* model/grid-height model/cell-size)) 2)]
    (.setColor g color)
    (.fillOval g
               (+ margin-x (* x model/cell-size) 10)
               (+ margin-y (* y model/cell-size) 10)
               30 30)))

(defn draw-miam
  "Draws the food item if it's visible."
  [g]
  (let [{:keys [miam miam-alive?]} @state/game-state]
    (when miam-alive?
      (when (nil? miam)
        (core/spawn-miam))
      (when miam
        (draw-entity g miam Color/MAGENTA)))))

(defn draw-game-over
  "Draws the game over screen."
  [g]
  (let [{:keys [score hi-score]} @state/game-state
        [r g-color b a] model/color-overlay
        [score-r score-g score-b] model/color-score-display
        center-x (/ model/zone-width 2)
        center-y (/ model/zone-height 2)]
    (.setColor g (Color. r g-color b a))
    (.fillRect g 0 0 model/zone-width model/zone-height)

    ;; Contour noir
    (.setFont g game-over-font)
    (.setColor g Color/BLACK)
    (.drawString g "GAME OVER" (- center-x 127) (- center-y 62)) ;; Légèrement décalé pour créer le contour

    ;; Texte rouge
    (.setColor g Color/RED)
    (.drawString g "GAME OVER" (- center-x 125) (- center-y 60))

    (.setFont g default-font)
    (.setColor g (Color. score-r score-g score-b))
    (.drawString g (str "Score: " score) (- center-x 100) center-y)
    (when (>= score hi-score)
      (.setColor g Color/YELLOW)
      (.drawString g (str "Hi-Score: " score) (- center-x 100) (+ center-y 60)))))

(defn create-game-panel
  "Creates the game panel."
  []
  (proxy [JPanel] []
    (paintComponent [g]
      (proxy-super paintComponent g)
      (draw-grid g)
      (draw-miam g)
      (let [{:keys [player enemy game-over?]} @state/game-state]
        (draw-entity g player Color/YELLOW)
        (draw-entity g enemy Color/CYAN)
        (when game-over?
          (draw-game-over g))))))

(defn create-key-listener
  "Creates a keyboard listener."
  [panel]
  (proxy [KeyListener] []
    (keyPressed [e]
      (let [key-code (.getKeyCode e)]
        (when (core/handle-key-press key-code #(.repaint panel))
          (.repaint panel))))
    (keyReleased [e])
    (keyTyped [e])))

(defn create-game-window
  "Creates the game window."
  []
  (let [frame (JFrame. "petitLapin V2")
        panel (create-game-panel)]
    (doto frame
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setResizable false)
      (.add panel))
    (.setPreferredSize panel (java.awt.Dimension. 400 600))
    (.pack frame)
    (doto panel
      (.setFocusable true)
      (.addKeyListener (create-key-listener panel)))
    (core/game-loop #(.repaint panel))
    (.setVisible frame true)
    frame))