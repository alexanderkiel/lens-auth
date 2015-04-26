(defproject lens-auth "0.1-SNAPSHOT"
  :description "Central OAuth 2.0 Authorization Server for Lens."

  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [prismatic/plumbing "0.4.0"]
                 [http-kit "2.1.18"]
                 [ring/ring-core "1.3.2"]
                 [bidi "1.18.10" :exclusions [ring/ring-core]]
                 [liberator "0.12.2"]
                 [io.clojure/liberator-transit "0.3.0"]]

  :profiles {:dev
             {:source-paths ["dev"]
              :dependencies [[org.clojure/tools.namespace "0.2.4"]
                             [criterium "0.4.3"]]
              :global-vars {*print-length* 20}}

             :production
             {:main lens.core}})
