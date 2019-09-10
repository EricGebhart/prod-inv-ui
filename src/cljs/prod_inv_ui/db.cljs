(ns prod-inv-ui.db)


(def default-db
  {:inventory nil
   :prod-choices nil

   :selected-view "single"

   :selected {:id nil
              :item nil
              :qty nil
              :history nil
              :line-chart nil}


   :server {:use false
            :path "http://localhost:5000/"
            :failure-result nil
            :result nil}

   :show-twirly nil

   :multi-line-chart nil})
