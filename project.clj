(defproject datomic-repro "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :repositories {"my.datomic.com" {:url      "https://my.datomic.com/repo"
                                   :username "oleh.kylymnyk@agiliway.com"
                                   :password "35b154c3-0572-40cb-8221-2373e798ef76"}}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.datomic/client-cloud "0.8.71"]
                 [com.climate/claypoole "1.1.4"]]
  :main ^:skip-aot datomic-repro.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
