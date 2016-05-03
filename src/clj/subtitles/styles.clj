(ns subtitles.styles
  (:require [garden.def :refer [defrule defstyles]]
            [garden.stylesheet :refer [rule]]))

(defstyles screen
  (let [body (rule :body)]
    [(body
     {:font-family "Helvetica Neue"
      :font-size   "16px"
      :line-height 1.5})
     [:.drop-area_empty
      {
       :position "fixed"
       :left "0"
       :right "0"
       :top "0"
       :bottom "0"
       :text-transform "uppercase"
       :justify-content "center"
       :display "flex"
       :align-items "center"
       :text-shadow "2px 0px 4px rgba(150, 150, 150, 1)"
       :box-shadow "inset 1px 1px 33px -10px rgba(0,0,0,0.75)"
       }]
     [:.drop-area
      {}]
     [:.subtitle {:font-family "Arial"
                  :font-size "14pt"}
      [:.text {:padding-left "1cm"} ]]
     [:.subtitle-titr
      [:.text {:font-weight "bold"
               :padding-left "3cm"} ]]
     ]))
