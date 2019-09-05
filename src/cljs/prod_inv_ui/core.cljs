(ns prod-inv-ui.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [prod-inv-ui.events :as events]
   [prod-inv-ui.views :as views]
   [prod-inv-ui.config :as config]
   ))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (enable-console-print!)
  (dev-setup)
  (mount-root))
