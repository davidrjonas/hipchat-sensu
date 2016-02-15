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

(def ^:private status->kw {0 :ok 1 :warn 2 :crit})

(defn- label-value [statuses]
  (let [warn-str (if (:warn statuses) (str "<b>" (:warn statuses) "</b>W ") "")
        crit-str (if (:crit statuses) (str "<b>" (:crit statuses) "</b>C ") "")]
    (str warn-str crit-str)))

(defn- lozenge-value [statuses]
  (cond-
    (:crit statuses) {:type "error" :label "ERROR"}
    (:warn statuses) {:type "current" :label "WARN"}
    :else {:type "success" :label "OK"}

(defn- status-freqs [events]
  (frequencies (map #(get status->kw (get-in % [:check :status])) events)))

(defn events->glance-data
  "Only one glance is handled"
  [events]
  (let [statuses (status-freqs events)]
    {:label {:type "html"
             :value (label-value statuses)}
     :status {:type "lozenge"
              :value (lozenge-value statuses)}
     }))

(defn wrap-sensu-glance [content]
  {:glance [{:key "sensu-glance"
             :content content}]})

(defn on-sensu-events [id events]
  (let [installation (inst/lookup id)
        url (str (:apiUrl installation) "addon/ui/room/" (:roomId installation))
        token (tok/lookup id)
        data (-> events events->glance-data wrap-sensu-glance)]
    (log/info "received events for " id ", " events)
    @(http/post-json url data token)))

