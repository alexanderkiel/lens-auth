(ns user
  (:require [clojure.pprint :refer [pprint pp]]
            [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer [refresh]]
            [com.stuartsierra.component :as comp]
            [environ.core :refer [env]]
            [lens.system :refer [new-system]]
            [schema.core :as s]))

(s/set-fn-validation! true)

(def system nil)

(defn init []
  (when-not system (alter-var-root #'system (constantly (new-system env)))))

(defn start []
  (alter-var-root #'system comp/start))

(defn stop []
  (alter-var-root #'system comp/stop))

(defn startup []
  (init)
  (start)
  (println "Server running at port" (:port (:server system))))

(defn reset []
  (stop)
  (refresh :after 'user/startup))

;; Init Development
(comment
  (startup)
  )

;; Reset after making changes
(comment
  (reset)
  )
