(ns hipchat-sensu.sensu
  (:require [hipchat-sensu.http :as http]
            [org.httpkit.timer :as timer]))

(def ^:private watchers (atom 0))
(def ^:private state (atom {}))

(def ^:private config (atom {:url "http://127.0.0.1:4567"
                             :poll-seconds 60}))

(defn- poll-interval-ms [] (* 1000 (:poll-seconds @config)))

(defn- poll-sensu []
  (reset! state (http/get-json (str (:url @config) "/events"))))

(defn set-config [new-config]
  (reset! config new-config))

(defn watch [id f]
  (let [watch-fn (fn [_key _ref _old new-state] (f id new-state))]
    (add-watch state id watch-fn)
    (swap! watchers inc)))

(defn unwatch [id]
  (remove-watch state id)
  (swap! watchers dec))

(defn current []
  (let [current @state]
    (if-not (empty? current)
      current
      (poll-sensu))))

(add-watch watchers :poller-on (fn [_ _ oldv newv]
                                 (if (and (= 0 oldv)
                                          (= 1 newv))
                                   (let [task (timer/schedule-task (poll-interval-ms) poll-sensu)]
                                     (add-watch watchers :poller-off (fn [_ _ oldv newv]
                                                                       (if (and (= 1 oldv)
                                                                                (= 0 newv))
                                                                         (timer/cancel task)
                                                                         (remove-watch watchers :poller-off))))))))


