(defproject service "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [http-kit "2.1.18"]
                 [org.clojure/data.json "0.2.6"]
                 [compojure "1.3.4"]
                 [environ "1.0.0"]
                 [clojurewerkz/neocons "3.0.0"]
                 [com.taoensso/timbre "3.4.0"]
                 [javax.servlet/servlet-api "2.5"]]
  :main service.core
  :profiles { 
             :dev {}
             :production {}})
