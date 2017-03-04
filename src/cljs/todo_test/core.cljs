(ns todo-test.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

(defonce todos (reagent/atom (sorted-map)))
(defonce counter (reagent/atom 0))

(defn add-todo [text]
  (let [id (swap! counter inc)]
    (swap! todos assoc id {:id id :title text :done false})))

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
 (let [editing (reagent/atom false)]
   (fn [{:keys [id done title]}]
     [:li
      [:div.view
       [:input.toggle {:type "checkbox"}]
       [:label {:on-double-click #(reset! editing true)} title]]])))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to todo-test"]
   [:div [:a {:href "/about"} "go to about page"]]
   (let [items (vals @todos)]
      [:section#todoapp
        [todo-input {:id "new-todo"
                     :placeholder "What needs to be done?"
                     :on-save add-todo}]
       (when (-> items count pos?)
          [:section#main
           [:ul#todo-list
            (for [todo items]
              ^{:key (:id todo)} [todo-item todo])]])])])

(defn about-page []
  [:div [:h2 "About todo-test"]
   [:div [:a {:href "/"} "go to the home page"]]])

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
