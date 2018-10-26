(ns grove.settings
  (:require [reagent.core :as r]
            [clojure.core.match :refer [match]]))

(def color-themes
  {"Exotica"
   {:background "#091423"
    :keyword "#66d9ef"
    :funname "#84b5ff"
    :placeholder "#c1caff"}})

(def default-settings
  {:color-theme "Exotica"
   :indent-size :medium
   :block-markers :braces})

(def settings default-settings)
