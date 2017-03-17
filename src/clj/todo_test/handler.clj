(ns todo-test.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [todo-test.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

;; The single source of truth, the database
;; {0 {:id 0 :text "a b c" :status "open"}}
(def todolist-atom (atom {}))

;; Okay, so we need a secondary truth for the id on each item
(def counter-atom (atom 0))

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

;; returns the dereferenced todolist-atom content
(defn list-get [] @todolist-atom)

(defn list-add
  [params]
  (let [id (swap! counter-atom inc)
        new-list-str (:list params)
        new-list (clojure.edn/read-string new-list-str)]
    (swap! todolist-atom
      (fn [todolist]
        (assoc todolist id (assoc new-list :id id))))))

(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/about" [] (loading-page))
  (GET "/list" [] (list-get))
  (POST "/list" request
    (list-add (:params request))

  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
