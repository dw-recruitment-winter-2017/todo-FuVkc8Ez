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

(def todolist (atom ""))

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")
     (swap! todolist println "take out more trash")]))

(defn list-get []
  (str "hello there"))

(defroutes routes
  (GET "/" request (loading-page))
  (GET "/about" [] (loading-page))
  (GET "/listget" [] (list-get))

  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
