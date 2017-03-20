(ns todo-test.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [ajax.core :refer [POST]]
              [ajax.core :refer [GET]]))

;; This enables JS console logging
(enable-console-print!)

;; This is our front end source of truth using the reagent/react version of an atom
(defonce state-atom (reagent/atom {}))

;; This is a holder for the text input contents
;; TODO: figure out why reagent/atom won't allow a nested map so this can live in the state-atom
(defonce text-atom (reagent/atom {}))

(defn handler [response]
  "This is the general success handler for the http requests"
  (reset! state-atom (cljs.reader/read-string response)))

(defn error-handler []
  "This is the general error handler for the http requests"
  (.log js/console (str "something bad happened")))


(defn add-todo []
  "This function adds a new todo item by POSTing to the backend and then updating the list with the response and resets the text input to nil"
  (let [text @text-atom]
    (POST "/add"
          {:params text
           :handler handler
           :error-handler error-handler})
    (reset! text-atom nil)))


(defn update-todo [id]
  "This function updates the todo item status"
  (POST "/update"
      {:params {:id id}
        :handler handler
        :error-handler error-handler}))

(defn remove-todo [id]
  "This function removes a todo from the list of todos"
  (POST "/remove"
    {:params {:id id}
     :handler handler
     :error-handler error-handler}))


;; The button triggers the click to fire the add-todo function
(defn todo-input [text]
  "This function handles the value in the input, initially set to nil"
  [:div
    [:input {:type "text" :value text :id "new-todo" :placeholder "What needs to be done?"
              :on-change #(reset! text-atom {:text (-> % .-target .-value)})}]
    [:button {:on-click #(add-todo)} "+"]])


;; On the checkbox click, update-todo is called, on the button click the item is deleted
(defn todo-item [{:keys [text complete id]}]
  "This function handles the todo items themselves"
  [:li {:id id :key id :class (if (true? complete) "complete" "no")}
    [:label
      [:input {:type "checkbox"
                :defaultChecked (true? complete)
                :on-click #(update-todo id)}]
      [:span text]]

    [:button {:on-click #(remove-todo id)} "x"]])




;; -------------------------
;; Views

;; The let portion does the following:
;; - binds the dereferenced state-atom to state
;; - binds the values of the todo list to items
;; - calls todo-input and passes in the state
;; - checks that there are todos in items
;; - as long as there are todo items, loops through each of them and passes that todo to todo-item to be rendered
(defn home-page []
  "This function renders the view for the home page"
  [:main
   [:div#heading [:h1 "To Do List"]]
   (let [state @state-atom, items (vals state), text-state @text-atom, text (vals text-state)]
      [:section#todoapp
        (todo-input text)
        (when (-> items count pos?)
          [:section#main
           [:ul#todo-list
            (for [todo items]
              (todo-item todo))]])])
   [:a {:href "/about"} "Learn more about the app >"]])


(defn about-page []
  "This function renders the view for the about page"
  [:main
   [:div#heading [:h2 "About your To Do List"] [:a {:href "/"} "Back to the list"]]
   [:p "This is a simple To Do list app built with Clojure using Reagent and Compojure with styling handled by Marx."]])


(defn current-page []
  "This function gets the current page and inserts it into a div"
  [:div [(session/get :current-page)]])


;; -------------------------
;; Routes
;; Both routes do the same thing, take the uri and apply the appropriate page

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))


;; -------------------------
;; Initialize app

;; This function will:
;; - GET /list the current state of the todolist-atom from the backend
;; - parse the result
;; - reset the state-atom with that parsed result
;; - render the current page
(defn mount-root []
  (GET "/list"
        {:handler (fn [data]
                    (let [updated-list (cljs.reader/read-string data)]
                      (println "data contents:" data)
                      (reset! state-atom updated-list)))
          :error-handler error-handler})
  (reagent/render [current-page] (.getElementById js/document "app")))


(defn init! []
  "This function initiates the whole app"
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
