(ns hipchat-sensu.http
  (:require [org.httpkit.client :as http]
            [taoensso.timbre :as log]
            [clojure.data.json :as json]))

(defn post-json [url json-data bearer-token]
  @(http/request {:method :post
                  :url url
                  :headers {:content-type "application/json"
                            :authorization (str "Bearer: " bearer-token)}
                  :body (json/write-str json-data)}))

(defn- response->json-params [response]
  (cond
    (:error response) response
    (>= (:status response) 400) {:error "failed to GET url"}
    :else
      (try
        (-> response :body (json/read-str :key-fn keyword))
        (catch java.io.EOFException e {:error "invalid json"}))))

(defn get-json [url]
  (let [response @(http/get url)]
    (assoc response :params (response->json-params response))))
