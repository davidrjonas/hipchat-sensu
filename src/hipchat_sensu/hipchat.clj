(ns hipchat-sensu.hipchat
  (:require [taoensso.timbre :as log]
            [clojure.string :as s]
            [hipchat-sensu.installation :as inst]
            [hipchat-sensu.access-token :as tok]
            [hipchat-sensu.http :as http]
            [ring.util.response :refer [response redirect get-header]]))

(def status->kw {0 :ok 1 :warn 2 :crit})

(defn label-value [statuses]
  (let [warn-str (if (:warn statuses) (str "<b>" (:warn statuses) "</b>W ") "")
        crit-str (if (:crit statuses) (str "<b>" (:crit statuses) "</b>C ") "")]
    (s/trim (str warn-str crit-str))))

(defn lozenge-value [statuses]
  (cond
    (:crit statuses) {:type "error" :label "ERROR"}
    (:warn statuses) {:type "current" :label "WARN"}
    :else {:type "success" :label "OK"}))

(defn status-freqs [events]
  (frequencies (map #(get status->kw (get-in % [:check :status])) events)))

(defn events->glance-data [events]
  (let [statuses (status-freqs events)]
    {:label {:type "html"
             :value (label-value statuses)}
     :status {:type "lozenge"
              :value (lozenge-value statuses)}
     }))

(defn wrap-sensu-glance [content]
  {:glance [{:key "sensu-glance" :content content}]})

(defn on-sensu-events [id events]
  (let [installation (inst/lookup id)
        url (str (:apiUrl installation) "addon/ui/room/" (:roomId installation))
        token (tok/lookup id)
        data (-> events events->glance-data wrap-sensu-glance)]
    (log/info "received events for " id ", " events)
    @(http/post-json url data token)))

