(ns prod-inv-ui.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [prod-inv-ui.core-test]))

(doo-tests 'prod-inv-ui.core-test)
