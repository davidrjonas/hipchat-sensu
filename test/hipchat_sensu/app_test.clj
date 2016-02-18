(ns hipchat-sensu.app-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [org.httpkit.fake :refer [with-fake-http]]
            [clojure.data.json :as json]
            [ring.util.response :refer [header get-header content-type]]
            [hipchat-sensu.app :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:body response) "HipChat Sensu Addon"))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404))))

  (testing "capabilities route"
    (let [response (app (mock/request :get "/capabilities.json"))]
      (is (= (:status response) 200))
      (is (= (get-header response "content-type") "application/json; charset=utf-8"))))

  (testing "glance route requires auth"
    (let [response (app (mock/request :post "/glance/0/data"))]
      (is (= (:status response) 401))
      (is (.startsWith (or (get-header response "www-authenticate") "") "Bearer realm="))
      (is (= (get-header response "content-type") "text/plain; charset=utf-8"))))

  (testing "/installed fetches capabilitiesUrl"
    (with-fake-http ["http://example.com" "{}"]
      (let [params (json/write-str {:capabilitiesUrl "http://example.com"})
            request (-> (mock/request :post "/installed" params) (header "content-type" "application/json"))
            response (app request)]
        (is (= (:status response) 200))))))

