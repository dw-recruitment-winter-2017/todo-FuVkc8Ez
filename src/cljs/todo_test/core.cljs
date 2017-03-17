(ns todo-test.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [ajax.core :refer [POST]]))

(defonce state-atom
 (reagent/atom
   {:todos {}
    :text nil}))

(defn add-todo [text]
  ;; POST to backend
  ;; improvement: add an error handler
  (POST "/list"
        {:params {0
          {:id 0
          :text "a b c"
          :open true}}
          :handler (fn [data]
            new-state (clojure.edn/read-string data)))})
  ;; ^ Take backend's response
  ;; Parse response with edn and put it in state-atom
  ;; swap! todos with new db value
  )

(defn todo-input [text]
  [:input {:type "text" :value text
           :id "new-todo" :placeholder "What needs to be done?"
           :on-change
           #(swap! state-atom
             (fn [state]
               (assoc state :text (-> % .-target .-value))))]
           ))

(defn todo-item [{:keys [title completed]}]
  [:li
    [:div.view
       [:input.toggle {:type "checkbox"}]
       [:label title]]])

;; -------------------------
;; Views

(defn home-page []
  [:main
   [:div#heading [:h2 "Welcome to your To Do List"]
                 [:a {:href "/about"} "Learn More"]]
   (let [state @state-atom
         items (vals (:todos state))]
      [:section#todoapp
        (todo-input state)
       (when (-> items count pos?)
          [:section#main
           [:ul#todo-list
            (for [todo items]
              (todo-item todo))]])])])

(defn about-page []
  [:main
   [:div#heading [:h2 "About your To Do List"] [:a {:href "/"} "Back to the list"]]
   [:p "This is a simple To Do list app built with Clojure using reagent and compojure."]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))


;; -------------------------
;; Initialize app

(defn mount-root []
  ;; GET /list
  ;; Parse the result
  ;; swap! your atom with that parsed result
  (reagent/render [current-page] (.getElementById js/document "app")))

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
