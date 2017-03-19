(ns todo-test.handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [todo-test.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]))

;; This is called into loading-page
(def mount-target
  "Placeholder and indicator rendered if figwheel is not running."
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  "This is a function that renders the hiccup template for the site"
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  "This is a function for the page rendering on load"
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

;; The single source of truth, the "database"
;; expected structure: {0 {:text "a b c" :complete true :id 0}}
(def todolist-atom (atom {}))

;; Okay, so we need a secondary truth for the id on each item
(def counter-atom (atom 0))

;; This function returns the dereferenced todolist-atom content
(defn list-get [] (str @todolist-atom))

;; This function
;; - accepts the params from the post request
;; - increments the counter-atom and binds it to id
;; - binds the :list from params to new-list-str
;; - converts new-list-str to edn syntax and binds it to new-list
;; - associates the incremented id into the new todo item
;; - associates the new todo item including the incremented id to the content of the todolist-atom
;; - swaps it into the todolist-atom
(defn list-add
  "This function accepts the params from the POST request and swaps it into the todolist-atom"
  [params]
  (let [id (swap! counter-atom inc)
        new-list-str (:list params)
        new-list (clojure.edn/read-string new-list-str)]
    (swap! todolist-atom
      (fn [todolist]
        (assoc todolist id (assoc new-list :complete false :id id))))))

;; Backend Routes
(defroutes routes
  ;; gets the html template for the home page
  (GET "/" [] (loading-page))

  ;; gets the html template for the about page
  (GET "/about" [] (loading-page))

  ;; gets the result of list-get
  (GET "/list" [] (list-get))

  ;; gives the request to the list-add function
  (POST "/list" request
    (list-add request))

  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
