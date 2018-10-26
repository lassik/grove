(ns grove.core
  (:require [reagent.core :as r]
            [clojure.core.match :refer [match]]
            [grove.settings]))

(declare body-component)
(declare function-component)
(declare upd!)

;;

(defn color [model color-name]
  ((grove.settings/color-themes (-> model :settings :color-theme))
   color-name))

(defn keyword-span [model keyword]
  [:span {:style {:color (color model :keyword)
                  :font-weight "bold"}}
   keyword])

;;

(defn init-model []
  {:settings grove.settings/default-settings
   :body [:begin
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

(defn int-component [model val]
  [:span val])

(defn call-component [model name args]
  (println)
  [:div name
   "("
   (into [:span] (interpose ", " (map #(body-component model %) args)))
   ")"])

(defn if-component [model params]
  (into [:div
         [:span {:style {:color (color model :keyword) :font-weight "bold"}} "if"] " "
         [:span {:style {:color (color model :placeholder)
                         :border (str "2px dashed " (color model :placeholder))}}
          "condition"]]
    (let [contents [:div {:style {:margin-left
                                  (case (-> model :settings :indent-width)
                                    :narrow "1em"
                                    :medium "2em"
                                    :wide "4em")}}
                    "then-block"]]
      (case (-> model :settings :block-markers)
        :indent
        contents
        :braces
        [" "
         [:span "{"]
         contents
         [:span "}"]]
        :begin-end
        [" "
         (keyword-span model "then")
         [:br]
         (keyword-span model "begin")
         contents
         (keyword-span model "end")]))))

(defn body-component [model body]
  (match body
    [:begin components] (into [:div] (map #(body-component model %) components))
    [:function params] (function-component model params)
    [:if params] (if-component model params)
    [:call name & args] (call-component model name args)
    [:int val] (int-component model val)))

(defn function-component [model function]
  [:div
   [:span {:style {:color (color model :keyword)
                   :font-weight "bold" :font-size "16px"}}
    "function "]
   (if (= :name (:selected function))
     [:input {:style {:background-color (color model :background)
                      :border "none"
                      :color (color model :funname) :font-size "16px"}
              :value (:name function)
              :on-change (fn [event]
                           (upd! :set-name (.. event -target -value)))
              :on-key-press (fn [event]
                              (println "key press" (.-charCode event))
                              (if (= 13 (.-charCode event))
                                (println "ENTER")
                                (println "NOT ENTER")))}]
     [:span {:style {:color (color model :funname)}} (:name function)])
   [:span " {"]
   [:div {:style {:margin-left "2em"}} (body-component model (:body function))]
   [:span "}"]])

(defn view-model [model]
  [:div
   [:span {:style {:color "gray"}} "Grove | "]
   [:span {} "example.c"]
   [:hr]
   (body-component model (:body model))])

;;

(def global-model (r/atom (init-model)))

(defn upd! [operation & args]
  (let [operation (into [operation] args)]
    (swap! global-model update-model operation)
    (println "global-model" @global-model)))

(defn ^:export run []
  (r/render [(fn [] (view-model @global-model))]
    (js/document.getElementById "app")))
