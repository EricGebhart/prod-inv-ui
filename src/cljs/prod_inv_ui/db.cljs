(ns prod-inv-ui.db)


(def default-db
  {
   :selected-view "single"
   :selected-id nil
   :selected-item nil
   :selected-qty nil
   :selected-hist nil
   :prod-choices nil
   :selected-group nil
   :inventory nil
   :remote-server-path "http://localhost:5000/"
   :use-server nil
   :show-twirly nil
   :failure-result nil
   :result nil
   :single-line-chart nil
   :multi-line-chart nil
   :test-chart {:chart {:type   :bar}
                :title  {:text "Chart title here"}
                :xAxis  {:categories ["Apples", "Bananas", "Oranges"]}
                :yAxis  {:title {:text "Fruit eaten"}}
                :series [{:name "Jane" :data [1, 0, 4]}
                         {:name "John" :data [5, 7, 3]}]}})
