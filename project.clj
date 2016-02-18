(defproject hipchat-sensu "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-json "0.4.0"]
                 [ring-logger-timbre "0.7.5"]
                 [com.taoensso/timbre "4.2.1"]
                 [org.clojure/data.json "0.2.6"]
                 [http-kit "2.1.18"]
                 [clj-time "0.11.0"]
                 [clj-jwt "0.1.1"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler hipchat-sensu.app/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
