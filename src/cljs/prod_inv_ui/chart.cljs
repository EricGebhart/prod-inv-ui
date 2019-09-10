(ns prod-inv-ui.chart
  (:require ["highcharts" :as hc]
            [reagent.core :as r]
            [re-frame.core :as rf]))

(def test-chart
  {:chart {:type   :bar}
   :title  {:text "Chart title here"}
   :xAxis  {:categories ["Apples", "Bananas", "Oranges"]}
   :yAxis  {:title {:text "Fruit eaten"}}
   :series [{:name "Jane" :data [1, 0, 4]}
            {:name "John" :data [5, 7, 3]}]})

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
             :pointFormat "{:point.x %e. %b} {:point.y .2f} m"}
   :plotOptions {:spline {:marker {:enabled true}}},
   :colors ["#6CF", "#39F", "#06C", "#036", "#000"],
   :series [{:name "Jane" :data [[1568063744825 41]
                                 [1568063741313 86]
                                 [1568063730500 28]
                                 [1568063705304 61]
                                 [1568063696140 93]]}
            {:name "John" :data [[1568063744825 41]
                                 [1568063741313 86]
                                 [1568063730500 28]
                                 [1568063705304 61]
                                 [1568063696140 93]]}]})


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

;; for now it only makes one kind of chart.
(defn line-chart-data
  "put the data into the line chart template"
  [data]
  (assoc line-chart :series data))
