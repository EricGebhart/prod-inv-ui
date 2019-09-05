(ns prod-inv-ui.events
  (:require [prod-inv-ui.events :as sut]
            [prod-inv-ui.subs :as subs]
            [re-frame :refer [dispatch-sync subscribe]]
            [cljs.test :as t :include-macros true]))



(deftest make-inv-record-test
  (let [rec (sut/make-inv-record 1 "foo")]
    (is (= (:id rec) 1))
    (is (= (:name rec) "foo"))
    (is  (int? (:inventory_level rec)) )
    (is  (int? (:timestamp rec)) )))

(deftest unique-products-test
  (let [inventory (repeatedly 10 (gen-inventory-items ["foo" "spam"]))
        unique-prods        (sut/unique inventory)]
    (is (= (count unique-prods) 2))))


(deftest build-chart-series-entry-test
  (let [inv-record {:id 1 :name "foo" :timesstamp 10 :inventory-level 2}
        data-record (build-chart-series-entry inv-record)]
    (is (= (count data-record) 2))
    (is (= (first data-record) 10))
    (is (= (second data-record) 2))
    (is (int? (first data-record)))
    (is (int? (last data-record)))))

(deftest build-single-chart-series-test
  (let [prod-hist (repeatedly 10 (gen-inventory-items ["spam"]))
        chart-series  (build-single-chart-series prod-hist)
        series-map (first chart-series)]
    (is (vector? chart-series))
    (is (map? series-map))
    (is (= (count (:data series-map)) 10))
    (is (= (:name series-map)) "spam")))

(deftest build-multi-chart-series-test
  (let [prod-hist (repeatedly 10 (gen-inventory-items ["spam" "foo" "bar"]))
        chart-series  (build-multi-chart-series prod-hist [1])
        series-map (first chart-series)]
    (is (vector? chart-series))
    (is (map? series-map))
    (is (= (count (:data series-map)) 10))
    (is (= (:name series-map)) "foo")))



;; the events in this project are really simple I'm not sure
;; that I want to bother testing them as they are almost always
;; just assigning values somewhere.  The complicated functions
;; are tested above.
;;
;; However, should it get more complicated this is a good way
;; to setup and test an event handler.
;;
;; use dispatch-sync to create the scenario and then use
;; subscribe to get to the data and see if it is what you expect.

;; setup - cummulatively build up db
(dispatch-sync [:initialise-db])
(dispatch-sync [:id-selected 2])

;; validate that the valuein 'app-db' is correct
;; perhaps with subscriptions
(deftest
  (let [selected-id (subscribe [::subs/selected-id])
        selecteed-item (subscribe [::subs/selected-item])
        prod-choices (subscribe [::subs/prod-choices])]
    (is (= selected-id 2))
    (is (= (:id selected-item) 2))
    (is (= (:name selected-item) "baz"))
    (count (= prod-choices 6))))


;; or another way is to just use a map which would work fine
;; for this project. this comes from the re-frame documentation.

;; (deftest
;;   (let [;; setup - cummulatively build up db
;;         db (-> {}    ;; empty db
;;                (initialise-db [:initialise-db])   ;; each event handler expects db and event
;;                (clear-panel   [:clear-panel])
;;                (draw-triangle [:draw-triangle 1 2 3]))

;; event  [:select-triange :other :stuff]

;; ;; now execute the event handler under test
;; db'    (select-triange db event)]

;; ;; validate that db' is correct
;; (is ...)))
