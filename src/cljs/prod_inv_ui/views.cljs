(ns prod-inv-ui.views
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [re-com.core
    :refer [h-box v-box box gap single-dropdown radio-button button alert-box
            input-text checkbox label title hyperlink-href p p-span throbber]]
   [prod-inv-ui.subs :as subs]
   [prod-inv-ui.events :as events]
   [re-frame-datatable.core :as dt]
   ["highcharts" :as hc]))


;;  A reagent highcharts component.

(defn mount-chart [comp]
  (hc/chart (r/dom-node comp) (clj->js (r/props comp))))

(defn update-chart [comp]
  (mount-chart comp))

(defn chart-inner []
  (r/create-class
   {:component-did-mount   mount-chart
    :component-did-update  update-chart
    :reagent-render        (fn [comp] [:div])}))

(defn chart-outer [config]
  [chart-inner @config])

(defn view-chart
  [subs]
  (let [chart-data (rf/subscribe subs)]
    (when (some? @chart-data)
      (fn []
        [chart-inner @chart-data]))))

;; basic form.

(defn app-title []
  [title
   :label "Product inventory history"
   :level :level1])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  Choose to use local random data in the SPA or connect to a server.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn use-server-checkbox
  "Checkbox to choose server data or local data"
  []
  (let [use-server (rf/subscribe [::subs/use-server])]
    [checkbox
     :model use-server
                                        ;:label "Use Server for data"
     :on-change #(rf/dispatch [::events/use-server %])]))

(defn server-path-input
  "Give the server address to connect to"
  []
  (let [server-path (rf/subscribe [::subs/remote-server-path])]
    [input-text
     :model            server-path
     ;; :status           @status
     ;; :status-icon?     @status-icon?
     ;; :status-tooltip   @status-tooltip
     :width            "300px"
     ;; :placeholder      (if @regex "enter number (99.9)" "placeholder message")
     :on-change        #(rf/dispatch [::events/change-server-path %])
     ;; :validation-regex @regex
     :change-on-blur?  true
     :disabled?        nil]))

(defn re-initialize-inventory []
  (let [show-twirly (rf/subscribe [::subs/show-twirly])]
    (fn [] [h-box
           :height   "50px"
           :gap      "50px"
           :align    :center
           :children [[button
                       :label             "Re-initialize the inventory"
                       :tooltip           "Re-initilize the inventory in-app or from a server"
                       :tooltip-position  :left-center
                       :on-click          #(rf/dispatch [::events/re-initialize-db])]
                      (when (some? @show-twirly) [throbber])]])))

(defn server-failure-alert []
  (let [failure (rf/subscribe [::subs/failure-result])]
    (fn [] (if (some? @failure)
            [:div
             [alert-box
              :alert-type :warning
              :body       (str @failure)
              :padding    "6px"
              :closeable? true
              :on-close   #(rf/dispatch [::events/reset-failure])]]))))


(defn server-box []
  [v-box
   :gap "10px"
   :width "600px"
   :align :center
   :children [[label :label "Use Server for data"]
              [ h-box
               :gap      "10px"
               :align    :center
               :children [[use-server-checkbox]
                          [server-path-input]
                          [re-initialize-inventory]
                          ]]
              [server-failure-alert]]])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  Multiple product view with select table, selected items preview and chart.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn product-multi-select
  "give a selectable table of unique products"
  []
  [dt/datatable
   :product-selection
   [::subs/prod-choices]
   [{::dt/column-key   [:id]
     ::dt/column-label "id"}
    {::dt/column-key   [:label]
     ::dt/column-label "Name"}]
   {::dt/table-classes ["ui" "table" "celled"]
    ::dt/selection {::dt/enabled? true}}])

(defn selected-rows-preview []
  (let [selected-items (rf/subscribe [::re-frame-datatable.core/selected-items
                                      :product-selection
                                      [::subs/prod-choices]])]
    (fn [] (do (rf/dispatch [::events/multi-select-items @selected-items])
              [:pre
               [:code @selected-items]]))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Single product view.  Drop down, add new level, history table, chart.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Just an experiment to see how boxes work.
(defn single-prod
  "show an product item record"
  [item]
  [h-box
   :gap      "10px"
   :children [[v-box
               :gap "5px"
               :children [[label :label "ID" ]
                          [label :label (:id item)]]]

              [v-box
               :gap "5px"
               :children [[label :label "Name"]
                          [label :label (:name item)]]]

              [v-box
               :gap "5px"
               :children [[label :label "Timestamp"]
                          [label :label (:timestamp item)]
                          ]]
              [v-box
               :gap "5px"
               :children [[label :label "Quantity"]
                          [label :label (:inventory_level item)]]]]])



(defn item-label
  "show a selected item or None"[]
  (let [selected-item (rf/subscribe [::subs/selected-item])]
    (fn [] (if (nil? @selected-item)
            [label :label "None"]
            (single-prod @selected-item)))))


(defn prod-select
  "give a single product dropdown, and show the current/last record for that product id."
  []
  (let [selected-id (rf/subscribe [::subs/selected-id])
        inventory (rf/subscribe [::subs/inventory])
        choices (rf/subscribe [::subs/prod-choices])]
    (fn [] [h-box
           :gap      "10px"
           :align    :center
           :children [[single-dropdown
                       :choices     choices
                       :model       selected-id
                       :title?      true
                       :placeholder "-- Choose a product --"
                       :width       "300px"
                       :max-height  "400px"
                       :filter-box? false
                       :on-change        #(rf/dispatch [::events/id-selected %])]
                      [h-box
                       :gap "10px"
                       :children [[:strong "Selected: "]
                                  [item-label]]]]])))

(defn input-product-level
  "Input a new product level"
  []
  (let [selected-qty (rf/subscribe [::subs/selected-qty])
        show-twirly (rf/subscribe [::subs/show-twirly])]
    (fn []
      [:div
       [:input {:type :number
                :value @selected-qty
                :on-change #(rf/dispatch [::events/qty-changed
                                          (-> % .-target .-value)])}]
       (when (some? @show-twirly) [throbber])])))

(defn update-level-button
  "button to create a new product entry"
  []
  [button
   :label             "Update new inventory level"
   :tooltip           "Creates a new inventory record for this product"
   :tooltip-position  :right-center
   :on-click #(rf/dispatch [::events/update-inv-level %])])

(defn add-new-inventory-level
  "give an opportunity to create a new inventory record for current product"
  []
  [h-box
   :gap "10px"
   :children [[label :label "Add new level"]
              [input-product-level]
              [update-level-button]]])

(defn product-history-table
  "show a table of the inventory history for the selected product id"
  []
  [dt/datatable
   :product-history
   [::subs/selected-hist]
   [{::dt/column-key   [:id]
     ::dt/column-label "id"}
    {::dt/column-key   [:name]
     ::dt/column-label "Name"}
    {::dt/column-key   [:timestamp]
     ::dt/column-label "Timestamp"}
    {::dt/column-key   [:inventory_level]
     ::dt/column-label "Quantity"}]
   {::dt/table-classes ["ui" "table" "celled"]}])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  the main view.  Either single product or multi-product view.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn single-view
  "show a product drop down, the current record for the selected id, and
  a table of it's inventory history, and a line chart of that history "
  []
  (let [selected-id (rf/subscribe [::subs/selected-id])]
    [v-box
     :gap "10px"
     :children [[prod-select]
                (when (some? @selected-id)
                  [v-box
                   :gap "10px"
                   :children [[add-new-inventory-level]
                              [product-history-table]]])
                ;;[test-chart]
                [view-chart [::subs/single-line-chart]]]]))

(defn multi-view
  "Show a table of selectable products and a line chart of their inventory history."
  []
  [v-box
   :children [[product-multi-select]
              [selected-rows-preview]
              [view-chart [::subs/multi-line-chart]]]])

(defn show-view
  "show either a single product selection view or a multiple product selection view"
  []
  (let [selected-view (rf/subscribe [::subs/selected-view])]
    (fn [] (if (= "single" @selected-view)
            (single-view)
            (multi-view)))))

(defn select-view
  "provide a radio button to choose between single product or multiple product views."
  []
  (let [selected-view (rf/subscribe [::subs/selected-view])]
    (fn []
      [v-box
       :width "500px"
       :align :center
       :children [[title :level :level2 :label "Choose single or multiple item view"]
                  [v-box
                   :children
                   [(doall (for [view-button ["single" "multi" ]]    ;; Notice the ugly "doall"
                             ^{:key view-button}                     ;; key should be unique among siblings
                             [radio-button
                              :label       view-button
                              :value       view-button
                              :model       selected-view
                              :label-style (if (= view-button @selected-view) {:font-weight "bold"})
                              :on-change   #(rf/dispatch [::events/selected-view %])]))]]]])))

(defn main-panel []
  [v-box
   :height "100%"
   :gap    "30px"
   :children [
              [app-title]
              [server-box]
              [select-view]
              [show-view]
              ]])
