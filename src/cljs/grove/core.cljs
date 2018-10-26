(ns grove.core
  (:require [reagent.core :as r]
            [clojure.core.match :refer [match]]))

(declare body-component)
(declare function-component)
(declare upd!)

(def color-background "#091423")
(def color-keyword "#66D9EF")
(def color-funname "#84B5FF")
(def color-placeholder "#C1CAFF")

;;

(defn init-model []
  {:body [:begin
          [[:function {:selected :name :name "increment" :body [:if {}]}]
           [:function {:name "update view"
                       :body [:begin
                              [[:if {}]
                               [:call "print" [:int 1] [:int 2] [:int 3]]]]}]]]})

(defn update-model [model operation]
  (println "upd" operation)
  (match operation
    [:set-name name]
    (update-in model [:function :name] (constantly name))))

(defn int-component [val]
  [:span val])

(defn call-component [name args]
  (println)
  [:div name
   "("
   (into [:span] (interpose ", " (map body-component args)))
   ")"])

(defn if-component [params]
  [:div
   [:span {:style {:color color-keyword :font-weight "bold"}} "if"] " "
   [:span {:style {:color color-placeholder :border (str "2px dashed " color-placeholder)}} "condition"]
   [:span " " "{"]
   [:div]
   [:span "}"]])

(defn body-component [body]
  (match body
    [:begin components] (into [:div] (map body-component components))
    [:function params] (function-component params)
    [:if params] (if-component params)
    [:call name & args] (call-component name args)
    [:int val] (int-component val)))

(defn function-component [function]
  [:div
   [:span {:style {:color color-keyword :font-weight "bold" :font-size "16px"}}
    "function "]
   (if (= :name (:selected function))
     [:input {:style {:background-color color-background
                      :border "none"
                      :color color-funname :font-size "16px"}
              :value (:name function)
              :on-change (fn [event]
                           (upd! :set-name (.. event -target -value)))
              :on-key-press (fn [event]
                              (println "key press" (.-charCode event))
                              (if (= 13 (.-charCode event))
                                (println "ENTER")
                                (println "NOT ENTER")))}]
     [:span {:style {:color color-funname}} (:name function)])
   [:span " {"]
   [:div {:style {:margin-left "2em"}} (body-component (:body function))]
   [:span "}"]])

(defn view-model [model]
  [:div
   [:span {:style {:color "gray"}} "Grove | "]
   [:span {} "example.c"]
   [:hr]
   (body-component (:body model))])

;;

(def global-model (r/atom (init-model)))

(defn upd! [operation & args]
  (let [operation (into [operation] args)]
    (swap! global-model update-model operation)
    (println "global-model" @global-model)))

(defn ^:export run []
  (r/render [(fn [] (view-model @global-model))]
    (js/document.getElementById "app")))
