(ns hipchat-sensu.app
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [taoensso.timbre :as log]
            [hipchat-sensu.handler :as handler]
            [hipchat-sensu.jwt :refer [wrap-jwt-auth]]
            [ring.util.response :refer [response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]))

; Use env to set vars
(def baseurl "http://localhost")

(def method->str {:head "HEAD" :get "GET" :post "POST" :put "PUT" :patch "PATCH"})

(defn wrap-logger [handler]
  (fn [request]
    (let [response (handler request)]
      (log/info (:remote-addr request)
                (str "\"" (-> request :request-method method->str) " " (:uri request) "\"")
                (:status response))
      (log/debug request)
      response)))

(defroutes public-routes
  (GET "/" [] "HipChat Sensu Addon")
  (GET "/info" request (str request))

  (GET "/capabilities.json"
       req (wrap-json-response
             (fn [req] (response (handler/capabilities baseurl)))))

  (POST "/installed"
        req (handler/installed (:params req)))

  (POST "/uninstalled"
        req (handler/uninstalled (:params req)))

  (route/not-found "Not Found"))

(defroutes jwt-routes
  (POST ["/glance/:glance/data", :glance #"[0-9]+"]
        req (wrap-json-response handler/glance-data)))

(def app
  (-> (routes
        (-> jwt-routes (wrap-routes wrap-jwt-auth))
        public-routes)
      (wrap-defaults api-defaults)
      wrap-logger
      wrap-json-params))
