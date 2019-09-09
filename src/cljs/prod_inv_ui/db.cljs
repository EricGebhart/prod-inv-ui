(ns prod-inv-ui.db)


(def default-db
  {
   :inventory nil
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

   :multi-line-chart nil

   :test-chart {:chart {:type   :bar}
                :title  {:text "Chart title here"}
                :xAxis  {:categories ["Apples", "Bananas", "Oranges"]}
                :yAxis  {:title {:text "Fruit eaten"}}
                :series [{:name "Jane" :data [1, 0, 4]}
                         {:name "John" :data [5, 7, 3]}]}})
