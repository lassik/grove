(ns grove.settings)

(def color-themes
  {"Exotica"
   {:background "#091423"
    :keyword "#66d9ef"
    :funname "#84b5ff"
    :string "#a6e22e"
    :placeholder "#c1caff"}})

(def default-settings
  {:color-theme "Exotica"
   :indent-width :medium
   :block-markers :braces})

(def settings default-settings)
