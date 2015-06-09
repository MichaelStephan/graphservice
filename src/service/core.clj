(ns service.core
  (:gen-class)
  (:require
    [taoensso.timbre :as log] 
    [environ.core :refer [env]]
    [org.httpkit.server :as srv]
    [clojure.string :as str]
    [clojure.data.json :as json]
    [clojurewerkz.neocons.rest :as nr]
    [clojurewerkz.neocons.rest.nodes :as nn]
    [clojurewerkz.neocons.rest.relationships :as nrl]
    [clojurewerkz.neocons.rest.cypher :as cy]
    [clojurewerkz.neocons.rest.records :as records])
  (:use
    [clojure.inspector]
    [compojure.route :only [files not-found]]
    [compojure.handler :only [site]]
    [compojure.core :only [defroutes GET POST DELETE ANY context]]))

(def neo4j-connection-string
  "http://localhost:7474/db/data/")

(def default-port 9000)

(def port (try
            (read-string (:port env))
            (catch Exception e 
              (log/warn "no PORT environment variable set, using default")
              default-port)))

(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn home []
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body "{}"})

(defn nodes []
  (let [conn (nr/connect neo4j-connection-string)
        queryresult (cy/tquery conn "MATCH (n) RETURN distinct(n)")]
    (pmap (fn[querynode]
           (let [node (first (vals querynode)) metadata (:metadata node) data (:data node)]
             {:id (:id metadata) 
              :name (:id data) 
              :labels (:labels metadata)
              :inbound (pmap (fn [relation]
                              {:start (last (str/split (:start relation) #"/"))
                               :type (:type relation)})
                             (nrl/incoming-for conn (records/instantiate-node-from node)))}))
          queryresult)))

(defn nodes->jsonnodes [nodes]
 {:nodes (pmap (fn [node]
                 {:id (:id node)
                  :nodeType (first (:labels node))
                  :caption (:name node)})
               nodes)

  :edges  (reduce into (pmap (fn [node]
                               (pmap (fn [inbound]
                                       {:source (:start inbound)
                                        :target (:id node)
                                        :edgeType (:type inbound)})
                                     (:inbound node)))
                             nodes))})

(defn query []
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str (nodes->jsonnodes (nodes)) :escape-slash false)})

(defroutes all-routes
  (GET "/" [] (home))
  (GET "/query" [] (query))
  (not-found {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "resource not found"}))

(defn -main []
  (log/info "server listening on port " port)
  (reset! server (srv/run-server #'all-routes {:port port})))
