(ns atlas.domain.sequence-diagram
  (:require [atlas.schemata.jaeger :as s-jaeger]
            [atlas.schemata.sequence-diagram :as s-sequence-diagram]
            [common-clj.schema.core :as cs]
            [java-time :as time]
            [schema.core :as s]))

(defn- microseconds->epoch [microseconds] (time/instant (/ microseconds 1000)))

(defn- span->end-time [{:keys [start-time duration]}]
  (let [start-epoch (microseconds->epoch start-time)]
    (time/plus start-epoch (time/millis duration))))

(defn- http-server-tag? [{:keys [value]}] (= "server" value))

(defn- http-client-tag? [{:keys [value]}] (= "client" value))

(defn- in-span? [{:keys [tags]}] (boolean (first (filter http-server-tag? tags))))

(defn- out-span? [{:keys [tags]}] (boolean (first (filter http-client-tag? tags))))

(defn- span->service-name [{:keys [process-id]} trace]
  (-> trace :processes process-id :service-name))

(defn- span->execution-box [trace]
  (fn [{:keys [span-id start-time duration] :as span}]
    {:id          span-id
     :start-time  (microseconds->epoch start-time)
     :duration-ms duration
     :lifeline    (span->service-name span trace)}))

(defn- child-of? [{:keys [span-id]}]
  (fn [{:keys [references]}]
    (boolean (first (filter #(= span-id (:span-id %)) references)))))

(defn- find-child [span trace]
  (->> trace :spans (filter (child-of? span)) first))

(defn- span->arrow-pair [trace]
  (fn [acc out-span]
    (if-let [child-span (find-child out-span trace)]
      (conj acc
            {:id         (:span-id out-span)
             :from       (span->service-name out-span trace)
             :to         (span->service-name child-span trace)
             :start-time (microseconds->epoch (:start-time out-span))}
            {:id         (:span-id child-span)
             :from       (span->service-name child-span trace)
             :to         (span->service-name out-span trace)
             :start-time (span->end-time out-span)})
      acc)))

(s/defn start-time :- cs/EpochMillis
  [trace :- s-jaeger/Trace]
  (->> trace :spans (map :start-time) sort first microseconds->epoch))

(s/defn duration-ms :- cs/PosInt
  [trace :- s-jaeger/Trace]
  (let [start-time (start-time trace)
        end-time (->> trace :spans (map span->end-time) sort last)]
    (.toMillis (time/duration start-time end-time))))

(s/defn lifelines :- [s-sequence-diagram/Lifeline]
  [trace :- s-jaeger/Trace]
  (->> trace :processes (map (fn [[_ {:keys [service-name]}]]
                               {:name service-name}))))

(s/defn execution-boxes :- [s-sequence-diagram/ExecutionBox]
  [trace :- s-jaeger/Trace]
  (let [in-spans (->> trace :spans (filter in-span?))]
    (map (span->execution-box trace) in-spans)))

(s/defn arrows :- [s-sequence-diagram/Arrow]
  [trace :- s-jaeger/Trace]
  (let [out-spans (->> trace :spans (filter out-span?))]
    (reduce (span->arrow-pair trace) [] out-spans)))
