(ns todo-test.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [ajax.core :refer [POST]]
              [ajax.core :refer [GET]]))
(enable-console-print!)
;; This is our front end source of truth using the reagent/react version of an atom
(defonce state-atom
 (reagent/atom {}))

;; This is a holder for the text input contents
;; TODO: figure out why reagent/atom won't allow a nested map so this can live in the state-atom
(defonce text-atom (reagent/atom "hello there"))


;; This function adds a new to do item by POSTing to the backend and then updating the list with the response
; (defn add-todo [text]
  ;; POST to backend
  ;; improvement: add an error handler
  ; (POST "/list"
  ;       {:params {0
  ;                 {:text "a b c"
  ;                  :open true}}
  ;         :handler (fn [data]
  ;                   new-state (clojure.edn/read-string data))}))
  ;; ^ Take backend's response
  ;; Parse response with edn and put it in state-atom
  ;; swap! todos with new db value


;; This function updates the status
;; improvement: expand this function or write a new one that will allow the
(defn update-todo [id])
  ;; POST to backend
  ;; Take backend's response
  ;; swap! todos with new db value


;; This function removes a todo from the list of todos
(defn remove-todo [id])
  ;; DELETE to backend
  ;; Take backend's response
  ;; Parse response
  ;; reset! todos with new db value


;; This function handles the value in the input, initially set to nil
;; The button triggers the click to fire the add-todo function
(defn todo-input [text]
  [:input {:type "text" :value text :id "new-todo" :placeholder "What needs to be done?"
           :on-change #(swap! state-atom
                          (fn [state]
                           ;; when you get a property from a javascript object in clojure it needs a dash
                           ;; we get targeted value from the state object that's been passed in
                           (assoc state :text (-> % .-target .-value))))}])
  ; [:button {:on-click}])
    ;; Takes the text, turn it into a todo data structure
    ;; resets text to nil


;; This function handles the todo items themselves.
;; On the checkbox click, update-todo is called
(defn todo-item [{:keys [text completed id]}]
  [:li {:id id :key id}
    [:div.view
       [:input.toggle {:type "checkbox"}]
       [:label text]]])

;; -------------------------
;; Views

;; This function renders the view for the home page
;; The let portion does the following:
;; - binds the dereferenced state-atom to state
;; - binds the values of the todo list to items
;; - calls todo-input and passes in the state
;; - checks that there are todos in items
;; - as long as there are todo items, loops through each of them and passes that todo to todo-item to be rendered
(defn home-page []
  [:main
   [:div#heading [:h2 "Welcome to your To Do List"]
                 [:a {:href "/about"} "Learn More"]]
   (let [state @state-atom, items (vals state), text-state @text-atom text (vals state)]
      [:section#todoapp
        (todo-input text)
        (when (-> items count pos?)
          [:section#main
            [:p "state"]
           [:ul#todo-list
            (for [todo items]
              (todo-item todo))]])])])

;; This function renders the view for the about page
(defn about-page []
  [:main
   [:div#heading [:h2 "About your To Do List"] [:a {:href "/"} "Back to the list"]]
   [:p "This is a simple To Do list app built with Clojure using reagent and compojure."]])

;; This function gets the current page and inserts it into a div
(defn current-page []
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
;; TODO: add an error handler
(defn mount-root []
  (GET "/list"
        {:handler (fn [data]
                    (let [updated-list (cljs.reader/read-string data)]
                      (println "data contents:" data)
                      (reset! state-atom updated-list)))})
  (reagent/render [current-page] (.getElementById js/document "app")))





;; This function initiates the whole app
(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
