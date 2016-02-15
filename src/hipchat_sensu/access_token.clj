(ns hipchat-sensu.access-token
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [hipchat-sensu.installation :as inst]
            [clj-time.core :as t]))

(def ^:private token-ttl-minutes 59)

(def ^:private access-token-store (atom {}))

(defn- store-set [oauth-id token]
  (swap! access-token-store assoc oauth-id token))

(defn- store-get [oauth-id]
  (get access-token-store oauth-id))

(defn- store-del [oauth-id]
  (swap! access-token-store dissoc oauth-id))

(defn- token-refresh-options [installation]
  {:url (:tokenUrl installation)
   :method :post
   :basic-auth [(:oauthId installation) (:oauthSecret installation)]
   :form-params {:grant_type "client_credentials"}})

(defn- next-expiry []
  (t/plus (t/now) (t/minutes token-ttl-minutes)))

(defn- refresh [oauth-id]
  (let [installation (inst/lookup oauth-id)
        resp @(http/request (token-refresh-options installation))
        token (json/read-str (:body resp))]
    (store-set oauth-id {:token token :expiry (next-expiry)})
    token))

(defn lookup [oauth-id]
  (let [installation (inst/lookup oauth-id)
        stored (store-get oauth-id)]
    (if (t/after? (:expiry stored) (t/now))
      (refresh oauth-id)
      (:token stored))))

(defn delete [oauth-id]
  (store-del oauth-id))

