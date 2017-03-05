(ns todo-test.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [ajax.core :refer [POST]]))

(defonce todos (reagent/atom (sorted-map)))
(defonce counter (reagent/atom 0))

(defn add-todo [text]
  (let [id (swap! counter inc)]
    (swap! todos assoc id {:id id :title text :done false})))
    ; (POST "/"){:params (:doc @todos) :handler (fn [_] (swap! todos assoc :saved? true))})

(defn save [id title] (swap! todos assoc-in [id :title] title))

(defn todo-input [{:keys [title on-save on-stop]}]
  (let [val (reagent/atom title)
        stop #(do (reset! val "")
                  (if on-stop (on-stop)))
        save #(let [v (-> @val str clojure.string/trim)]
                (if-not (empty? v) (on-save v))
                (stop))]
    (fn [{:keys [id class placeholder]}]
      [:input {:type "text" :value @val
               :id id :class class :placeholder placeholder
               :on-blur save
               :on-change #(reset! val (-> % .-target .-value))
               :on-key-down #(case (.-which %)
                               13 (save)
                               27 (stop)
                               nil)}])))

(defn todo-item []
   (fn [{:keys [title]}]
     [:li
      [:div.view
       [:input.toggle {:type "checkbox"}]
       [:label title]]]))

;; -------------------------
;; Views

(defn home-page []
  [:main
   [:div#heading [:h2 "Welcome to your To Do List"][:a {:href "/about"} "Learn More"]]
   (let [items (vals @todos)]
      [:section#todoapp
        [todo-input {:id "new-todo"
                     :placeholder "Add a To Do"
                     :on-save add-todo}]
       (when (-> items count pos?)
          [:section#main
           [:ul#todo-list
            (for [todo items]
              ^{:key (:id todo)} [todo-item todo])]])])])

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
