
(ns edu.ucdenver.ccp.kr.dev.cider-nrepl
  "In which is defined the mechanisms for starting an nREPL server."
  (:gen-class)
  (:refer-clojure :exclude [format])
  (:require [clojure.tools.nrepl.server :refer [start-server]]
            [cider.nrepl :refer (cider-nrepl-handler)]))

(defn -main
  [& args]
  (start-server :port 4005 :handler cider-nrepl-handler))
