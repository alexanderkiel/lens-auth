(ns lens.route)

(defn routes [context-path]
  [(if (= "/" context-path) "" context-path)
   {"/token" :token
    "/authorization" :authorization
    "/introspect" :introspect
    "/sign-in" :sign-in}])
