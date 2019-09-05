(ns prod-inv-ui.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::test
 (fn [db]
   (:test db)))

(re-frame/reg-sub
 ::selected-id
 (fn [db]
   (:selected-id db)))

(re-frame/reg-sub
 ::selected-item
 (fn [db]
   (:selected-item db)))

(re-frame/reg-sub
 ::selected-qty
 (fn [db]
   (:selected-qty db)))

(re-frame/reg-sub
 ::selected-hist
 (fn [db]
   (:selected-hist db)))

(re-frame/reg-sub
 ::prod-choices
 (fn [db]
   (:prod-choices db)))

(re-frame/reg-sub
 ::selected-group
 (fn [db]
   (:selected-group db)))

(re-frame/reg-sub
 ::inventory
 (fn [db]
   (:inventory db)))

(re-frame/reg-sub
 ::selected-view
 (fn [db]
   (:selected-view db)))

(re-frame/reg-sub
 ::use-server
 (fn [db]
   (:use-server db)))

(re-frame/reg-sub
 ::remote-server-path
 (fn [db]
   (:remote-server-path db)))

(re-frame/reg-sub
 ::failure-result
 (fn [db]
   (:failure-result db)))

(re-frame/reg-sub
 ::show-twirly
 (fn [db]
   (:show-twirly db)))

(re-frame/reg-sub
 ::single-line-chart
 (fn [db]
   (:single-line-chart db)))

(re-frame/reg-sub
 ::multi-line-chart
 (fn [db]
   (:multi-line-chart db)))

(re-frame/reg-sub
 ::test-chart
 (fn [db]
   (:test-chart db)))
