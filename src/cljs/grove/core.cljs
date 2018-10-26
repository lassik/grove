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

(defn indent-em [model]
  (case (-> model :settings :indent-width)
    :narrow "1em"
    :medium "2em"
    :wide "4em"))

(defn keyword-span [model keyword]
  [:span {:style {:color (color model :keyword)
                  :font-weight "bold"}}
   keyword])

(defn placeholder-span [model hint]
  [:span {:style {:color (color model :placeholder)
                  :border (str "2px dashed " (color model :placeholder))}}
   hint])

(defn body-stuff [model body]
  (let [indented-body
        [:div {:style {:margin-left (indent-em model)}}
         body]]
    (case (-> model :settings :block-markers)
      :indent
      [indented-body]
      :braces
      [" "
       [:span "{"]
       indented-body
       [:span "}"]]
      :begin-end
      [" "
       (keyword-span model "then")
       [:br]
       (keyword-span model "begin")
       indented-body
       (keyword-span model "end")])))

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
  (into [:div (keyword-span model "if") " " (placeholder-span model "condition")]
    (body-stuff model (placeholder-span model "then this code"))))

(defn body-component [model body]
  (match body
    [:begin components] (into [:div] (map #(body-component model %) components))
    [:function params] (function-component model params)
    [:if params] (if-component model params)
    [:call name & args] (call-component model name args)
    [:int val] (int-component model val)))

(defn function-component [model function]
  (into [:div
         (keyword-span model "function")
         " "
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
           [:span {:style {:color (color model :funname)}} (:name function)])]
    (body-stuff model (body-component model (:body function)))))

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
