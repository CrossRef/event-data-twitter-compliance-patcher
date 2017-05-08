(ns event-data-twitter-compliance-patcher.core-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as clj-time]
            [event-data-twitter-compliance-patcher.core :as core]))

(def input-event
  {"license" "https://creativecommons.org/publicdomain/zero/1.0/"
   "obj_id" "https://doi.org/10.1007/s00266-017-0820-4"
   "source_token" "45a1ef76-4f43-4cdc-9ba8-5a6ad01cc231"
   "occurred_at" "2017-02-27T19:06:32Z"
   "subj_id" "http://twitter.com/Ulcerasnet/statuses/836291446168817665"
   "id" "00001916-bbf6-4698-b8b8-26dbd885afa9"
   "evidence_record" "https://evidence.eventdata.crossref.org/evidence/2017022710f43213-2c97-4fd0-a934-77082a688b89"
   "terms" "https://doi.org/10.13003/CED-terms-of-use"
   "action" "add"
   "subj" {
    "pid" "http://twitter.com/Ulcerasnet/statuses/836291446168817665"
    "title" "Tweet 836291446168817665"
    "issued" "2017-02-27T19:06:32.000Z"
    "author" {
      "url" "http://www.twitter.com/Ulcerasnet"
      }
      "original-tweet-url" "http://twitter.com/UrgoTouch_es/statuses/836203902781517829"
      "original-tweet-author" "http://www.twitter.com/UrgoTouch_es"
      "alternative-id" "836291446168817665"}
    "source_id" "twitter"
    "obj" {
        "pid" "https://doi.org/10.1007/s00266-017-0820-4"
        "url" "http://link.springer.com/article/10.1007/s00266-017-0820-4"}
        "timestamp" "2017-02-27T19:07:05Z"
        "relation_type_id" "discusses"})

(def expected-event
  {"license" "https://creativecommons.org/publicdomain/zero/1.0/"
   "obj_id" "https://doi.org/10.1007/s00266-017-0820-4"
   "source_token" "45a1ef76-4f43-4cdc-9ba8-5a6ad01cc231"
   "occurred_at" "2017-02-27T19:06:32Z"
   ; Subject ID is a Tweet ID, which is "content". So should be truncated.
   "subj_id" "http://twitter.com"
   "id" "00001916-bbf6-4698-b8b8-26dbd885afa9"
   "evidence_record" "https://evidence.eventdata.crossref.org/evidence/2017022710f43213-2c97-4fd0-a934-77082a688b89"
   "terms" "https://doi.org/10.13003/CED-terms-of-use"
   "action" "add"
   ; Whole of subj should be removed.
   "source_id" "twitter"
   "obj" {
    "pid" "https://doi.org/10.1007/s00266-017-0820-4"
    "url" "http://link.springer.com/article/10.1007/s00266-017-0820-4"}
    "timestamp" "2017-02-27T19:07:05Z"
    "relation_type_id" "discusses"
    ; Updated fields should be added.
    "updated" "deleted",
    "updated_reason" "https://evidence.eventdata.crossref.org/announcements/2017-05-08T08-41-00Z-CED-9.json",
    "updated_date" "2017-01-01T00:00:00Z"})

(deftest patch-event-should-make-transformations
  (testing "patch-event should remove and update relevant fields."
    (clj-time/do-at (clj-time/date-time 2017 01 01)
      (is (= (core/patch-event input-event) expected-event)))))


