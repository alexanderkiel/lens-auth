(ns lens.route)

(defn routes [context-path]
  [(if (= "/" context-path) "" context-path)
   {"/token" :token
    "/introspect" :introspect}])
