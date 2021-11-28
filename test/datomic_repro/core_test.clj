(ns datomic-repro.core-test
  (:require
   [clojure.test :refer :all]
   [datomic-repro.core :refer :all]
   [datomic.client.api :as d]
   [com.climate.claypoole :as cp]))

(def NUMBER-OF-TESTS 5000)

(def CONCURRENCY 50)

(def db-options
  {:server-type :ion
   :region      "us-east-1"
   :system      "datomic-videra-dev"
   :topology    :production
   :endpoint    "http://entry.datomic-videra-dev.us-east-1.datomic.net:8182/"
   :proxy-port  8182})

(def sample-tx
  [{:db/id "new-rating"
    :rating/answer 637716745493701
    :rating/evaluator 10150691347780673
    :rating/scale 598134326722178
    :rating/session 637716745492608
    :rating/rating "3"}])

(defn exec-test [conn]
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
        (is (= [true false] [created? deleted?]))))))

(deftest a-test
  (testing "FIXME, I fail."
    (let [client (d/client db-options)
          conn (d/connect client {:db-name "videra"})]
      (cp/pdoseq CONCURRENCY [_ (range NUMBER-OF-TESTS)]
                 (exec-test conn)))))
