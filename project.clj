(defproject event-data-twitter-compliance-patcher "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojure "1.8.0"]
                 [event-data-common "0.1.25"]
                 [org.clojure/data.json "0.2.6"]
                 [crossref-util "0.1.10"]
                 [overtone/at-at "1.2.0"]
                 [robert/bruce "0.8.0"]
                 [yogthos/config "0.8"]
                 [clj-time "0.12.2"]
                 [http-kit "2.1.18"]
                 [clj-http "2.3.0"]]
  :main ^:skip-aot event-data-twitter-compliance-patcher.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
