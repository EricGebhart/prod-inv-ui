(ns prod-inv-ui.subs
  (:require
   [re-frame.core :as re-frame]))


(re-frame/reg-sub
 ::selected-id
 (fn [db]
   (get-in db [:selected :id])))

(re-frame/reg-sub
 ::selected-item
 (fn [db]
   (get-in db [:selected :item])))

(re-frame/reg-sub
 ::selected-qty
 (fn [db]
   (get-in db [:selected :qty])))

(re-frame/reg-sub
 ::selected-hist
 (fn [db]
   (get-in db [:selected :history])))

(re-frame/reg-sub
 ::prod-choices
 (fn [db]
   (:prod-choices db)))

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
   (get-in db [:server :use])))

(re-frame/reg-sub
 ::remote-server-path
 (fn [db]
   (get-in db [:server :path])))

(re-frame/reg-sub
 ::failure-result
 (fn [db]
   (get-in db [:server :failure-result])))

(re-frame/reg-sub
 ::show-twirly
 (fn [db]
   (:show-twirly db)))

(re-frame/reg-sub
 ::single-line-chart
 (fn [db]
   (get-in db [:selected :line-chart])))

(re-frame/reg-sub
 ::multi-line-chart
 (fn [db]
   (:multi-line-chart db)))

(re-frame/reg-sub
 ::test-chart
 (fn [db]
   (:test-chart db)))
