(defproject lens-auth "0.1-SNAPSHOT"
  :description "Central OAuth 2.0 Authorization Server for Lens."
  :url "https://github.com/alexanderkiel/lens-auth"

  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.json "0.2.6"]
                 [environ "1.0.0"]
                 [org.clojure/tools.reader "0.9.2"]
                 [prismatic/plumbing "0.4.4"]
                 [http-kit "2.1.18"]
                 [ring/ring-core "1.3.2"]
                 [pathetic "0.5.1"]
                 [bidi "1.20.2" :exclusions [org.clojure/clojurescript
                                             com.cemerick/clojurescript.test]]
                 [liberator "0.13"]]

  :profiles {:dev
             {:source-paths ["dev"]
              :dependencies [[org.clojure/tools.namespace "0.2.4"]
                             [criterium "0.4.3"]]
              :global-vars {*print-length* 20}}

             :production
             {:main lens.core}})
