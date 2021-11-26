(ns datomic-repro.core-test
  (:require
   [clojure.test :refer :all]
   [datomic-repro.core :refer :all]
   [datomic.client.api :as d]
   [com.climate.claypoole :as cp]))

(def NUMBER-OF-TESTS 5000)

(def CONCURRENCY 40)

(def schema
  [{:db/ident       :rating/answer
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "answer id (Could be session id or q-group id)"}

   {:db/ident       :rating/evaluator
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "evaluator id"}

   {:db/ident       :rating/scale
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "scale id"}

   {:db/ident       :rating/rating
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "rating"}

   {:db/ident       :rating/session
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "Session which the rating belongs to"}

   {:db/ident       :session/code
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident       :answer/text
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident       :scale/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident       :evaluator/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}])

(deftest a-test
  (testing "FIXME, I fail."
    (let [client (d/client {:server-type :ion
                            :region      "us-east-1"
                            :system      "datomic-videra-dev"
                            :topology    :production
                            :endpoint    "http://entry.datomic-videra-dev.us-east-1.datomic.net:8182/"
                            :proxy-port  8182})]
      (d/create-database client {:db-name "oleh-datomic-repro"})
      (let [conn (d/connect client {:db-name "oleh-datomic-repro"})]
        (d/transact conn {:tx-data schema})
        (let [result (d/transact conn {:tx-data [{:db/id "evaluator"
                                                  :evaluator/name "evaluator"}
                                                 {:db/id "answer"
                                                  :answer/text "answer"}
                                                 {:db/id "scale"
                                                  :scale/name "scale"}
                                                 {:db/id "session"
                                                  :session/code "session"}]})
              answer-id (-> result :tempids (get "answer"))
              evaluator-id (-> result :tempids (get "evaluator"))
              scale-id (-> result :tempids (get "scale"))
              session-id (-> result :tempids (get "session"))
              sample-tx [{:db/id "new-rating"
                          :rating/answer answer-id
                          :rating/evaluator evaluator-id
                          :rating/scale scale-id
                          :rating/session session-id
                          :rating/rating "3"}]]
          (cp/pdoseq CONCURRENCY [_ (range NUMBER-OF-TESTS)]
                     (let [db (d/with-db conn)
                           {:keys [tx-data db-after db-before]} (d/with db {:tx-data sample-tx})
                           grouped-data (group-by :e (rest tx-data))]
                       (doseq [eid (keys grouped-data)]
                         (let [created? (empty? (d/q
                                                 '[:find ?eid
                                                   :in $ ?eid
                                                   :where
                                                   [?eid]]
                                                 db-before eid))
                               deleted? (empty? (d/q
                                                 '[:find ?eid
                                                   :in $ ?eid
                                                   :where
                                                   [?eid]]
                                                 db-after eid))]
                           (is (= [true false] [created? deleted?])))))))))))
