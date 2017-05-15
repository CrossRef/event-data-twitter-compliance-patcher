(ns event-data-twitter-compliance-patcher.core
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [event-data-common.backoff :as backoff]
            [clj-time.core :as clj-time]
            [clj-time.format :as clj-time-format]
            [clj-time.periodic :as clj-time-periodic]
            [event-data-common.storage.store :as store]
            [event-data-common.storage.store :refer [Store]]
            [event-data-common.storage.s3 :as s3]
            [event-data-common.date :as date]
            [config.core :refer [env]]
            [org.httpkit.client :as client]
            [robert.bruce :refer [try-try-again]]))

(def date-format
  (:date-time-no-ms clj-time-format/formatters))

(def ymd-format (clj-time-format/formatter "yyyy-MM-dd"))

(def announcement-url
  "A static URL that describes the Twitter compliance activity"
  "https://evidence.eventdata.crossref.org/announcements/2017-05-08T08-41-00Z-CED-9.json")

(def storage
  (delay (s3/build (:s3-key env) (:s3-secret env) (:s3-region-name env) (:s3-bucket-name env))))

(defn patch-event
  "Update the given Event structure."
  [event]
  (-> event
    (assoc "subj_id" "http://twitter.com")
    (dissoc event "subj")
    (assoc "updated" "deleted")
    (assoc "updated_reason" announcement-url)
    (assoc "updated_date" (clj-time-format/unparse date-format (clj-time/now)))))

(defn update-event-id
  "Retrieve, patch and save the Event with the given ID."
  [event-id]
  (let [url (str (:event-bus-url-base env) "/events/" event-id)
        response (client/get url
                             {:headers {"Content-Type" "application/json"
                                        "Authorization" (str "Bearer " (:jwt-token env))}})
        event (-> response deref :body json/read-str)
        patched (patch-event event)]

    (try-try-again
      ; Exception thrown if not 200 or 201, also if some other exception is thrown during the client posting.
      #(let [response @(client/put url {:headers {"Content-Type" "application/json"
                                                   "Authorization" (str "Bearer " (:jwt-token env))}
                                         :body (json/write-str patched)})]

          (when-not (#{201 200} (:status response)) (throw (new Exception (str "Failed to send to Event Bus with status code: " (:status response) (:body response)))))))))


(defn first-compliance-log-chunk
  "Process a compliance log chunk"
  []
  (let [batch-key (last (store/keys-matching-prefix @storage "twitter/tweet-deletions/"))
        _ (log/info "Looking at batch" batch-key)
        ids (json/read-str (store/get-string @storage batch-key))
        num-ids (count ids)
        chunks (partition-all 100 ids)
        num-tweet-ids (atom 0)
        num-matching-tweet-ids (atom 0)

        ; Filter those tweet IDs that are connected to any Event.
        ; In chunks.
        filtered-by-exist-chunks (remove empty?
                            (pmap (fn [ids]
                               (let [response (client/get (str (:query-api-base env) "/special/alternative-ids-check") {:as :text :query-params {:ids (clojure.string/join "," ids)}})
                                     matched-ids (-> response deref :body (json/read-str :key-fn keyword) :alternative-ids)]
                                (swap! num-tweet-ids #(+ % (count ids)))
                                (swap! num-matching-tweet-ids #(+ % (count matched-ids)))
                                (when (zero? (rem @num-tweet-ids 100000))
                                  (log/info "Matched" @num-matching-tweet-ids "from checked" @num-tweet-ids "from total" num-ids "=" (int (* (/ @num-tweet-ids num-ids) 100)) "%"))
                                matched-ids)) chunks))


        ; Find all Event IDs that correspond to Tweet IDs. Some may already have been deleted.
        ; In chunks.
        event-ids-chunks (remove empty?
                           (pmap (fn [ids]
                                   (mapcat (fn [id]
                                     (let [response (client/get (str (:query-api-base env) "/events") {:as :text :query-params {:filter (str "source:twitter,alternative-id:" id)}})
                                            events (-> response deref :body (json/read-str :key-fn keyword) :message :events)]
                                       (map :id events))) ids))
                                 filtered-by-exist-chunks))

        event-ids (apply concat event-ids-chunks)]

    (log/info "Looking in" batch-key)
    (doall filtered-by-exist-chunks)
    (doall event-ids-chunks)

    (log/info "Interested in tweet IDs" (apply concat filtered-by-exist-chunks))
    (log/info "Interested in" (count event-ids) "event IDs" event-ids)

    (doseq [event-id event-ids]
      (log/info "Patching event id" event-id)
      (update-event-id event-id))

    ; Only get here if there were no errors.
    (log/info "Deleting batch key" batch-key)
    (store/delete @storage batch-key)))

(defn compliance-log-all
  []
  (loop [i 1]
    (first-compliance-log-chunk)
    (log/info "Done" i "log chunks.")
    (Thread/sleep 10000)
    (recur (inc i))))

(defn from-local-text-file
  "Take a text file with newline-separated IDs, patch all Event IDs."
  [filename]
  (let [ids (.split (slurp filename) "\n")]
    (doseq [id ids]
      (log/info "Event ID" id)
      (update-event-id id))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (condp = (first args)
    "from-local-text-file" (from-local-text-file (second args))
    "from-log" (compliance-log-all)
    (log/error "Didn't recognise command")))

