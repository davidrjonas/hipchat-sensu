(ns hipchat-sensu.jwt
  (:require [taoensso.timbre :as log]
            [clj-jwt.core :as jwt]
            [clojure.string :as s]
            [ring.util.response :refer [get-header]]
            [hipchat-sensu.installation :as inst]))

(defn safe-str->jwt [encoded-token]
  (try
    (jwt/str->jwt encoded-token)
    (catch Exception e
      (log/info "failed to parse jwt:" (.getMessage e))
      {})))

(defn validate-jwt-token [encoded-token]
  (let [token (safe-str->jwt encoded-token)
        oauth-id (:iss token)
        secret (-> oauth-id inst/lookup :oauthSecret)]
    (if (and token oauth-id secret)
      (and (jwt/verify token secret) (:claims token)))))

(defn validate-request [request]
  (let [token (or (-> request :params :signed_request)
                  (s/replace-first (or (get-header request "authorization") "") #"Bearer " ""))]
    (if-not (empty? token) (validate-jwt-token token))))

(defn unauthorized
  ([] (unauthorized "none"))
  ([realm]
   {:status 401
    :headers {"WWW-Authenticate" (str "Bearer realm=\"" realm "\"")
              "Content-type" "text/plain"}
    :body "401 Unauthorized"}))

(defn wrap-jwt-auth [handler]
  (fn [request]
    (if-let [claims (validate-request request)]
      (handler (assoc request :claims claims))
      (unauthorized "hipchat-sensu"))))

