(ns prod-inv-ui.events
  (:require
   [cljs-time.core :as time]
   [cljs-time.coerce :as tc]
   [re-frame.core :refer [reg-event-db reg-event-fx dispatch subscribe]]
   [prod-inv-ui.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [ajax.core :as ajax]
   [day8.re-frame.http-fx]))

(def product-names ["foo" "bar" "baz" "green eggs" "ham" "spam"])

(defn assoc-in-multi [m path keys values]
  (reduce #(assoc-in %1 (into path (key %2)) (val %2))
          m
          (zipmap keys values)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Build line charts from data.
;; Would be nice to re-factor into a more useful charting function.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def line-chart
  {:chart {:type   :spline}
   :title  {:text "Product inventory level"}
   :subtitle {:text "irregular time data"}
   :xAxis  {:type "datetime"
            :dateTimeLabelFormats {:month "%e. %b" :year "%b"}
            :title {:text "Date"}}
   :yAxis  {:title {:text "Inventory Level"}
            :min 0}
   :tooltip {:headerFormat "<b> {series.name} </b><br>"
             :pointFormat "{:point.x %e. %b} {:point.y .2f} m"
             }
   :plotOptions {:spline {:marker {:enabled true}}},
   :colors ["#6CF", "#39F", "#06C", "#036", "#000"],
   :series [{:name "Jane" :data [1, 0, 4]}
            {:name "John" :data [5, 7, 3]}]})

(defn line-chart-data
  "put the data into the line chart template"
  [data]
  (assoc line-chart :series data))

(defn build-chart-series-entry
  "create a chart series entry from a product history record."
  [hist-record]
  [(:timestamp hist-record) (:inventory_level hist-record)])

(defn build-single-chart-series
  "build the data records for one series, ie. one product."
  [selected-rows]
  [{:name (str \" (:name (first selected-rows)) \")
    :data (into []
                (map build-chart-series-entry selected-rows))}])

(defn build-multi-chart-series
  "build the chart data records for multiple ids."
  [inventory selected-ids]
  (->> (filter (fn [item] (some #(= (:id item) %) selected-ids)) inventory)
       (group-by :id)
       (map val)
       (map #(first (build-single-chart-series %)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; create some random inventory
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-inv-record [id name]
  "create a product record using a slightly random time."
  {:id id
   :name name
   :timestamp (+ (tc/to-long (time/now)) (rand-int 100000))
   :inventory_level (rand-int 101)} )

(defn gen-inventory-items
  "generate a set of inventory records one each for a list of names"
  [prod-names]
  (map #(make-inv-record %1 %2) (range)  prod-names))

(defn unique-products
  "create a list of unique product names and ids."
  [inv]
  (->> inv
       (map #(into {} {:id (:id %) :label (:name %)}))
       distinct
       (sort-by :id)))

(defn gen-inventory
  "generate a history of inventory records"
  [prod-names]
  (->> (fn [] (gen-inventory-items prod-names))
       (repeatedly 10)
       (mapcat identity)
       (sort-by :timestamp)
       reverse))  ;; so the latest date is first.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;   initialize the db events.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; switching between server and non-server modes.
;; reinitialize the inventory
(reg-event-db
 ::re-initialize-db
 (fn-traced [db _]
            (let [use-server (:use-server db)]
              (if (nil? use-server)
                (let [inv (gen-inventory product-names)]
                  (-> db
                      (assoc :inventory inv
                             :prod-choices (unique-products inv)
                             :multi-line-chart nil)
                      (assoc-in  [:selected :id] nil)
                      (assoc-in  [:selected :item] nil)
                      (assoc-in  [:selected :qty] nil)
                      (assoc-in  [:selected :hist] nil)
                      (assoc-in  [:selected :line-chart] nil)))
                (do (dispatch [:http-inventory])
                    db)))))

(reg-event-db
 ::initialize-db
 (fn-traced [_ _]
            (let [inv (gen-inventory product-names)]
              (assoc db/default-db
                     :inventory inv
                     :prod-choices (unique-products inv)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; xhrio.  Get inventory or add a new inventory level record.
;; the post would work for a new product too.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(reg-event-fx                           ;; note the trailing -fx
 :http-inventory                        ;; usage:  (dispatch [:http-inventory])
 (fn [{:keys [db]} [_ _]]                    ;; the first param will be "world"
   (let [server (str (:remote-server-path db) "api/products")]
     {:db   (assoc db :show-twirly true)   ;; causes the twirly-waiting-dialog to show??
      :http-xhrio {:method          :get
                   :uri             server
                   :timeout         8000                                           ;; optional see API docs
                   :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                   :on-success      [::success-http-result]
                   :on-failure      [::failure-http-result]}})))

(reg-event-fx
 ::http-add
 (fn [{:keys [db]} [_ new-item]]
   (let [server (str (:remote-server-path db) "api/products")]
     {:db (assoc db :show-twirly true)  ;; We don't need this if we operate optimistically.
      :http-xhrio {:method          :post
                   :uri             server
                   :params          new-item
                   :timeout         5000
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::success-post-result]
                   :on-failure      [::failure-post-result]}})))

(reg-event-db
 ::success-http-result
 (fn [db [_ result]]
   (assoc db
          :inventory result
          :prod-choices (unique-products result)
          :show-twirly nil)))

(reg-event-db
 ::failure-http-result
 (fn [db [_ result]]
   (-> db
       (assoc :show-twirly nil)
       (assoc-in [:server :failure-result] result))))

(reg-event-db
 ::success-post-result
 (fn [db [_ result]]
   (let [new-inv (conj (:inventory db) result)]
     (dispatch [::single-selected-hist (get-in db [:selected :id])])
     (-> db
         (assoc :inventory (into [result] (:inventory db))
                :show-twirly nil)
         (assoc-in [:selected :item] result)
         (assoc-in [:server :result] result)))))

(reg-event-db
 ::failure-post-result
 (fn [db [_ result]]
   (-> db
       (assoc :show-twirly nil)
       (assoc-in [:server :failure-result] result))))

(reg-event-db
 ::reset-failure
 (fn [db [_ _]]
   (assoc-in db [:server :failure-result] nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;   The usual db manipulation events.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(reg-event-db
 ::selected-view
 (fn-traced [db [_ selected-view]]
            (assoc db :selected-view selected-view)))

(defn item-for-id
  "get the newest record for an id."
  [db id]
  (first (filter #(= id (:id %)) (:inventory db))))

;; A product was selected. update everything and create a chart.
(reg-event-db
 ::id-selected
 (fn-traced [db [_ new-prod-selection]]
            (let [item (item-for-id db new-prod-selection)
                  qty (:inventory_level item)]
              (dispatch [::single-selected-hist new-prod-selection])
              (-> db
                  (assoc-in [:selected :item] item)
                  (assoc-in [:selected :qty] qty)
                  (assoc-in [:selected :id] new-prod-selection)))))

;; set the product history and build a chart.
(reg-event-db
 ::single-selected-hist
 (fn-traced [db [_ id]]
            (let [selected-hist (filter #(= id (:id %) ) (:inventory db))]
              (-> db
                  (assoc-in [:selected :history] selected-hist)
                  (assoc-in [:selected :line-chart] (-> selected-hist
                                                        build-single-chart-series
                                                        line-chart-data))))))

(reg-event-db
 ::use-server
 (fn-traced [db [_ use-server]]
            (assoc-in db [:server :use] use-server)))

(reg-event-db
 ::change-server-path
 (fn-traced [db [_ server-path]]
            (assoc-in db [:server :path] server-path)))

(reg-event-db
 ::qty-changed
 (fn-traced [db [_ qty]]
            (assoc-in db [:selected :qty] (int qty))))

;; I'm sure theres a more idiomatic way to do this.
(reg-event-db
 ::update-inv-level
 (fn-traced [db [_ new-level]]
            (let [use-server (:use-server db)
                  item (get-in db [:selected :item])
                  name (:name item)
                  id (:id item)
                  qty (get-in db [:selected :qty])
                  new-item (into item {:inventory_level qty :timestamp (str (time/now))})]
              (if (nil? use-server)
                (do (dispatch [::single-selected-hist id])
                    (-> db
                        (assoc :inventory (into [new-item] (:inventory db)))
                        (assoc-in [:selected :item] new-item)))
                (do (dispatch [::http-add {:name name :id id :inventory_level qty}])
                    db)))))

;; build chart data when multi-select has items.
(reg-event-db
 ::multi-select-items
 (fn-traced [db [_ items]]
            (assoc db :multi-line-chart
                   (->> (map :id items)
                        (build-multi-chart-series (:inventory db))
                        line-chart-data))))
