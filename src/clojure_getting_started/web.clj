(ns clojure-getting-started.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]))

(defn add-shutdown-hook []
  (.addShutdownHook (Runtime/getRuntime) (Thread. (fn [] (println "Shutting down...")))))

(defn list-shutdown-hooks []
  (let [clazz (Class/forName "java.lang.ApplicationShutdownHooks")
        field (.getDeclaredField clazz "hooks")]
      (.setAccessible field true)
      (println (.get field nil))))

(defn splash []
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello from Heroku"})

(defroutes app
  (GET "/" []
       (splash))
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn -main [& [port]]
  (add-shutdown-hook)
  (list-shutdown-hooks)
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
