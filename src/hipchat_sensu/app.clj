(ns hipchat-sensu.app
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.logger.timbre :as ring.logger]
            [hipchat-sensu.handler :as handler]
            [hipchat-sensu.jwt :refer [wrap-jwt-auth]]
            [ring.util.response :refer [response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]))

; Use env to set vars
(def baseurl "http://localhost")

(defroutes jwt-routes
  (POST ["/glance/:glance/data", :glance #"[0-9]+"]
        req (wrap-json-response handler/glance-data)))

(def all-routes
  (routes
    (GET "/" [] "HipChat Sensu Addon")
    (GET "/info" request (str request))

    (GET "/capabilities.json"
         req (wrap-json-response
               (fn [req] (response (handler/capabilities baseurl)))))

    (POST "/installed"
          req (handler/installed (:params req)))

    (POST "/uninstalled"
          req (handler/uninstalled (:params req)))

    (wrap-routes jwt-routes wrap-jwt-auth)

    (route/not-found "Not Found")))

(def app
  (-> all-routes
      (wrap-defaults api-defaults)
      ring.logger/wrap-with-logger
      wrap-json-params))
