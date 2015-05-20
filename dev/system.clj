(ns system
  (:use plumbing.core)
  (:require [clojure.string :as str]
            [org.httpkit.server :refer [run-server]]
            [lens.app :refer [app]]
            [lens.util :refer [parse-int]])
  (:import [java.io File]))

(defn env []
  (if (.canRead (File. ".env"))
    (->> (str/split-lines (slurp ".env"))
         (reduce (fn [ret line]
                   (let [vs (str/split line #"=")]
                     (assoc ret (first vs) (str/join "=" (rest vs))))) {}))
    {}))

(defn system [env]
  {:app app
   :context-path (or (env "CONTEXT_PATH") "/")
   :version (System/getProperty "lens-auth.version")
   :port (or (some-> (env "PORT") (parse-int)) 5000)})

(defnk start [app port & more :as system]
  (let [stop-fn (run-server (app (assoc more :db (atom {}))) {:port port})]
    (assoc system :stop-fn stop-fn)))

(defn stop [{:keys [stop-fn] :as system}]
  (stop-fn)
  (dissoc system :stop-fn))
