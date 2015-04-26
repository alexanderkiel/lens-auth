(ns system
  (:require [org.httpkit.server :refer [run-server]]
            [lens.app :refer [app]]
            [lens.util :refer [parse-int]]))

(defn env []
  {})

(defn system [env]
  {:app app
   :version (System/getProperty "lens.version")
   :port (or (some-> (env "PORT") (parse-int)) 5003)})

(defn start [{:keys [app db-uri version port] :as system}]
  (let [stop-fn (run-server (app (atom {})) {:port port})]
    (assoc system :stop-fn stop-fn)))

(defn stop [{:keys [stop-fn] :as system}]
  (stop-fn)
  (dissoc system :stop-fn))
