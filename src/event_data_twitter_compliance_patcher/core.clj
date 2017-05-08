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
            [org.httpkit.client :as client]))

(def date-format
  (:date-time-no-ms clj-time-format/formatters))

(def announcement-url
  "A static URL that describes the Twitter compliance activity"
  "https://evidence.eventdata.crossref.org/announcements/2017-05-08T08-41-00Z-CED-9.json")

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

    (backoff/try-backoff
      ; Exception thrown if not 200 or 201, also if some other exception is thrown during the client posting.
      #(let [response @(client/put url {:headers {"Content-Type" "application/json"
                                                   "Authorization" (str "Bearer " (:jwt-token env))}
                                         :body (json/write-str patched)})]

          (when-not (#{201 200} (:status response)) (throw (new Exception (str "Failed to send to Event Bus with status code: " (:status response) (:body response))))))
      10000
      5
      ; Only log info on retry because it'll be tried again.
      #(log/info "Error sending Event" (:id event) "with exception" (.getMessage %))
      ; But if terminate is called, that's a serious problem.
      #(log/error "Failed to send Event" (:id event) "to downstream")
      #(log/debug "Finished broadcasting" (:id event) "to downstream"))))

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
    (log/error "Didn't recognise command")))

