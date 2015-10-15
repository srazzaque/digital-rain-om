(ns ^:figwheel-always matrix-react.core
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [sablono.core :as html :refer-macros [html]]
              [cljs.core.async :refer [chan timeout]])
    (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(def num-cols 50)
(def max-rows 30)
(def charset "qwertyuiopasdfghjklzxcvbnm1234567890-={}[]:;'<>,.>?!@#$%^&*()")

(defonce app-state
  (atom {:columns (vec (for [_ (range 0 num-cols)] {:letters [" "]
                                                    :next-char-pos -1}))}))

(defn rand-letter
  []
  (.charAt charset (rand (.-length charset))))

(defn add-letter-to-column
  [column]
  (let [next-char-pos (mod (inc (:next-char-pos column)) max-rows)
        letters (:letters column)]
    (assoc
     column
     :next-char-pos next-char-pos
     :letters (assoc letters next-char-pos (rand-letter)))))

(defn populate-next-letter
  []
  (swap! app-state update-in [:columns (rand num-cols)] add-letter-to-column))

(defn letter
  [data]
  (reify
    om/IRender
    (render [_]
      (html
       [:div {:className "Letter Letter--fadeOut"
              :key data}
        data]))))

(defn digital-rain
  [data owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (go-loop []
        (<! (timeout (rand 50)))
        (populate-next-letter)
        (recur)))
    om/IRender
    (render [_]
      (html
       [:div {:className "ColumnHolder"}
        (for [column (:columns @data)]
          [:div {:className "Column"}
           (om/build-all letter (:letters column))])]))))

(om/root
 digital-rain
 app-state
 {:target (. js/document (getElementById "app"))})

(defn on-js-reload []
   ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

