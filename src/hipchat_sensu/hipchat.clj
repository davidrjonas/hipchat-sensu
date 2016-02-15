(ns hipchat-sensu.hipchat
  (:require [taoensso.timbre :as log]
            [clojure.data.json :as json]
            [hipchat-sensu.installation :as inst]
            [hipchat-sensu.access-token :as tok]
            [hipchat-sensu.http :as http]
            [hipchat-sensu.sensu :as sensu]
            [ring.util.response :refer [response redirect]]))

(comment
(defn- validate-jwt [oauth-id token]
  (let [secret (-> oauth-id inst/lookup :oauthSecret)
        jwt (str->jwt token )]
    (verify jwt secret) (:claims jwt)))
)

(defn events->glance-data [events])

(defn wrap-sensu-glance [content]
  {:glance [{:key "sensu-glance"
             :content content}]})

(defn on-sensu-events [id events]
  (let [installation (inst/lookup id)
        url (str (:apiUrl installation) "addon/ui/room/" (:roomId installation))
        token (tok/lookup id)
        data (wrap-sensu-glance (events->glance-data events))]
    (log/info "received events for " id ", " events)
    @(http/post-json url data token)))

