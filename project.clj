(defproject lens-auth "0.1-SNAPSHOT"
  :description "Central OAuth 2.0 Authorization Server for Lens."
  :url "https://github.com/alexanderkiel/lens-auth"

  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/tools.reader "0.9.2"]
                 [prismatic/plumbing "0.4.3"]
                 [http-kit "2.1.18"]
                 [ring/ring-core "1.3.2" :exclusions [commons-codec]]
                 [pathetic "0.5.1"]
                 [bidi "1.18.11" :exclusions [org.clojure/clojurescript]]
                 [liberator "0.12.2"]
                 [com.cognitect/transit-clj "0.8.271"]
                 [io.clojure/liberator-transit "0.3.0"]]

  :profiles {:dev
             {:source-paths ["dev"]
              :dependencies [[org.clojure/tools.namespace "0.2.4"]
                             [criterium "0.4.3"]]
              :global-vars {*print-length* 20}}

             :production
             {:main lens.core}})
