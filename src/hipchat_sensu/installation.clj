(ns hipchat-sensu.installation)

(def store (atom {}))

(defn add [oauth-id m]
  (swap! store assoc oauth-id m))

(defn lookup [oauth-id]
  (get @store oauth-id))

(defn delete [oauth-id]
  (swap! store dissoc oauth-id))

