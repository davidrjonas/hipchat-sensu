(ns hipchat-sensu.handler
  (:require [taoensso.timbre :as log]
            [clojure.data.json :as json]
            [hipchat-sensu.installation :as inst]
            [hipchat-sensu.access-token :as tok]
            [hipchat-sensu.http :as http]
            [hipchat-sensu.sensu :as sensu]
            [hipchat-sensu.hipchat :as hipchat]
            [ring.util.response :refer [response redirect]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]))

(defn capabilities [baseurl]
  {:name "Sensu HipChat AddOn"
   :description "Sensu glances and side-view"
   :key ""
   :vendor {:name "No-IP" :url "http://www.noip.com"}
   :links {:homepage "http://gitlab.lax/djonas/hipchat-sensu-addon"
           :self (str baseurl "/capabilities.json")}
   :capabilities {:hipchatApiConsumer {}
                  :installable {:allowGlobal true
                                :allowRoom true
                                :callbackUrl (str baseurl "/installed")
                                :uninstalledUrl (str baseurl "/uninstalled")}
                  :glance [{:icon {:url ""
                                   "url@2x" ""}
                           :key "sensu-glance"
                           :name {:value "Sensu Glance"}
                           :queryUrl (str baseurl "/glance-data")
                           :target "sensu-sidebar"}]}})


(defn installed [params]
  (let [capa (:params (http/get-json (:capabilitiesUrl params)))
        params (assoc params :capabilities capa)
        id (:oauthId params)]
    (inst/add id params)
    (sensu/watch id hipchat/on-sensu-events)
    (log/info "installed: " params))
  {:status 200})

(defn uninstalled [params]
  (let [redirectUrl (:redirect-url params)
        installableUrl (:installable-url params)
        installation (:params (http/get-json installableUrl))]
    (-> installation :oauthId inst/delete tok/delete)
    (log/info "uninstalled: " params ", installation: " installation)
    (redirect redirectUrl)))

(defn glance-data [request]
  (comment validate-jwt)
  (comment ensure cors)
  (response (hipchat/events->glance-data (sensu/current))))
