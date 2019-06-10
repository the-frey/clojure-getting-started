(ns clojure-getting-started.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [hiccup.core :as h]
            [clojure.data.csv :as csv])
  (:import java.lang.Integer))

(def default-file-location "./data/wordlist.csv")

(defn load-wordlist-file [path-with-extension]
  (with-open [reader (io/reader path-with-extension)]
    (doall
     (csv/read-csv reader))))

(defn wordlist-numbered-mapping [file-location]
  (reduce (fn [acc i]
            (assoc acc (Integer/parseInt (first i)) (second i)))
          {}
          (load-wordlist-file file-location)))

(defn dice-roll->word [hash-map dice-roll]
  (get hash-map
       dice-roll))

(defn roll-dice []
  (-> (rand-int 6)
      inc))

(defn roll-multiple-dice [num-dice]
  (->> (repeatedly roll-dice)
       (take num-dice)))

(defn multiple-dice->string [dice-coll]
  (->> dice-coll
       (map str)
       clojure.string/join))

(defn roll-x-dice-y-times [x y]
  (->> (repeatedly (partial roll-multiple-dice
                            x))
       (take y)))

(defn generate-pass-phrase []
  (let [word-mapping (wordlist-numbered-mapping default-file-location)
        six-sets-of-five-rolls (roll-x-dice-y-times 5 6)]
    (->> six-sets-of-five-rolls
         (map multiple-dice->string)
         (map #(Integer/parseInt %))
         (map #(dice-roll->word word-mapping
                                %)))))

(defn splash []
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (h/html [:div
                  [:h1 "Your passphrase is:"]
                  [:p (map #(str % " ")
                           (generate-pass-phrase))]])})

(defroutes app
  (GET "/" []
       (splash))
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
