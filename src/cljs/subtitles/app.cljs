(ns subtitles.app
  (:require [reagent.core :as reagent :refer [atom]]
            [clojure.string :as string]))


(defn tab []
  [:span {:dangerouslySetInnerHTML {:__html "&nbsp;"}}])

(defmulti chunk->html :name)

(defmethod chunk->html :default
  [{:keys [id text time name]}]
  [:div.subtitle {:key id}
   [:div.when time]
   (if-not (= "ШМИКТОР" name) [:div.name name])
   [:div.text text]])

(defmethod chunk->html "ТИТР"
  [{:keys [id text time name]}]
  [:div.subtitle.subtitle-titr {:key id}
   time " " name
   [:span.text text]])

(defn time->short
  [time]
  (let [start (subs time 0 8)
        hour (subs start 0 2)]
    (if (= "00" hour)
      (subs start 3)
      start)))

(defn text->chunk
  [text]
  (let [[[number time name] txt] (split-at 3 (string/split-lines (string/trim text) ))
        text-body (string/join txt)]
    {:id number
     :time (time->short time)
     :name name
     :text text-body}))

(defn process
  [text]
  (let [text->html (comp chunk->html text->chunk)
        converter (partial map text->html)]
    (-> text
        (string/split #"\n\r")
        converter)))

(def app-state (reagent/atom {:text "Drop .srt here"
                              :class "drop-area drop-area_empty"}))

(defn on-drop [e]
  (.preventDefault e)
  (.stopPropagation e)
  (let [file (goog.object/get (-> e .-dataTransfer .-files) 0)
        rdr (js/FileReader.)]
    (set! (.-onload rdr)
          (fn [e]
            (let [content (.-result (.-target e))]
              (swap! app-state assoc :text (process content)
                                      :class "drop-area drop-area_full"))))
    (.readAsText rdr file "windows-1251")))

(defn drop-area-render []
  (let [text (:text @app-state)
        class (:class @app-state)]
    [:div {:class class} text]))

(defn drop-area-did-mount [this]
  (.addEventListener js/document "drop" on-drop false)
  (.addEventListener js/document "dragover" #(.preventDefault %) false))

(defn drop-area []
  (reagent/create-class
   {:reagent-render drop-area-render
    :component-did-mount drop-area-did-mount}))

(defn init []
  (reagent/render [drop-area]
                  (.getElementById js/document "container")))
