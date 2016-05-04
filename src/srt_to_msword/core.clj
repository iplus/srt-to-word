(ns srt-to-msword.core
  (:import [org.apache.poi.xwpf.usermodel XWPFDocument XWPFParagraph XWPFRun XWPFStyle])
  (:import [java.io FileOutputStream FileInputStream IOException FileDescriptor InputStream File])
  (:import [javax.swing JFileChooser])
  (:import [javax.swing.filechooser FileNameExtensionFilter])
  (:import [java.awt Desktop])
  (:use [clojure.java.io])
  (:require [clojure.string :as string])
  (:gen-class))

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

(defmulti chunk->p (fn [_ a] (:name a)))

(defmethod chunk->p "ТИТР"
  [doc {:keys [time name text]}]
  (let [p (.createParagraph doc)]
    (doto (.createRun p) (.setFontFamily "Arial") (.setFontSize 14) (.setText time))
    (doto (.createRun p) (.setFontFamily "Arial") (.setFontSize 14) (.setText " "))
    (doto (.createRun p) (.setFontFamily "Arial") (.setFontSize 14) (.setText name))
    (doto (.createRun p) (.setFontFamily "Arial") (.setFontSize 14) (.addTab))
    (doto (.createRun p) (.setFontFamily "Arial") (.setFontSize 14) (.addTab))
    (doto (.createRun p) (.setFontFamily "Arial") (.setFontSize 14) (.addTab))
    (doto (.createRun p) (.setFontFamily "Arial") (.setFontSize 14) (.setBold true) (.setText text))))

(defmethod chunk->p :default
  [doc {:keys [time name text]}]
  (doto (-> doc .createParagraph .createRun)
    (.setFontFamily "Arial")
    (.setFontSize 14)
    (.setText time))
  (if-not (= "ШМИКТОР" name)
    (doto (-> doc .createParagraph .createRun)
      (.setFontFamily "Arial")
      (.setFontSize 14)
      (.setText name)))
  (if-not (= "" text)
    (doto (.createRun (doto (.createParagraph doc)
                        (.setIndentFromLeft 600)))
      (.setFontFamily "Arial")
      (.setFontSize 14)
      (.setText text))))

(defn text->docx
  [doc text]
  (let [text->doc (comp (partial chunk->p doc) text->chunk)
        chunks (-> text
                   (string/split #"\n\r"))
        children (->> chunks
                      (map string/trim)
                      (filter (complement (partial = ""))))]
    (doseq [chunk children] (text->doc chunk)))
  doc)

(defn tlt-get-file [ ]
  (let [ extFilter (FileNameExtensionFilter. "Text File" (into-array  ["srt"]))
        filechooser (JFileChooser. ".")
        dummy (.setFileFilter filechooser extFilter)
        retval (.showOpenDialog filechooser nil) ]
    (if (= retval JFileChooser/APPROVE_OPTION)
      (.getPath (.getSelectedFile filechooser) )
      nil)))

(defn -main
  [& args]
  (if-let [file (tlt-get-file)]
    (let [fout (string/replace file #"\.srt$" ".docx")]
      (with-open [out (FileOutputStream. fout)]
        (.write (text->docx (XWPFDocument. (clojure.java.io/input-stream (clojure.java.io/resource "template.docx")) )
                            (slurp file :encoding "windows-1251"))
                out))
      (comment .open
       (Desktop/getDesktop)
       (File. fout)))))
