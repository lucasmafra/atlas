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

(defn- match-tag?
  ([k] #(= k (:key %)))
  ([k v] (every-pred #(= k (:key %)) #(= v (:value %)))))

(defn- find-tag
  ([k tags] (->> tags (filter (match-tag? k)) first))
  ([k v tags] (->> tags (filter (match-tag? k v)) first)))

(def has-tag? (comp boolean find-tag))

(defn- server-span? [{:keys [tags]}] (has-tag? "span.kind" "server" tags))

(defn- consumer-span? [{:keys [tags]}] (has-tag? "span.kind" "consumer" tags))

(defn- client-span? [{:keys [tags]}] (has-tag? "span.kind" "client" tags))

(defn- producer-span? [{:keys [tags]}] (has-tag? "span.kind" "producer" tags))

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
    (->> references (filter #(= span-id (:span-id %))) first boolean)))

(defn- find-child [span trace] (->> trace :spans (filter (child-of? span)) first))

(defn- span->http-method [{:keys [tags]}] (->> tags (find-tag "http.method") :value))

(defn- span->http-url [{:keys [tags]}] (->> tags (find-tag "http.url") :value))

(defn- process->lifeline [[_ {:keys [service-name]}]] {:name service-name})

(defn- span->topic [{:keys [tags]}] (->> tags (find-tag "message_bus.destination") :value))

(defn- topic->lifeline [topic] {:name topic})

(defn- span->arrow-pair [trace]
  (fn [acc out-span]
    (if-let [child-span (find-child out-span trace)]
      (conj acc
            {:id         (:span-id out-span)
             :from       (span->service-name out-span trace)
             :to         (span->service-name child-span trace)
             :start-time (microseconds->epoch (:start-time out-span))
             :prefix     (span->http-method out-span)
             :label      (span->http-url out-span)}
            {:id         (:span-id child-span)
             :from       (span->service-name child-span trace)
             :to         (span->service-name out-span trace)
             :start-time (span->end-time out-span)
             :label      "response"})
      acc)))

(s/defn start-time :- cs/EpochMillis
  [trace :- s-jaeger/Trace]
  (->> trace :spans (map :start-time) sort first microseconds->epoch))

(s/defn duration-ms :- cs/PosInt
  [trace :- s-jaeger/Trace]
  (let [start-time (start-time trace)
        end-time   (->> trace :spans (map span->end-time) sort last)]
    (.toMillis (time/duration start-time end-time))))

(s/defn lifelines :- [s-sequence-diagram/Lifeline]
  [{:keys [spans processes]} :- s-jaeger/Trace]
  (let [services (->> processes (map process->lifeline))
        topics   (->> spans (filter producer-span?) (map span->topic) (map topic->lifeline))]
    (concat services topics)))

(s/defn execution-boxes :- [s-sequence-diagram/ExecutionBox]
  [trace :- s-jaeger/Trace]
  (let [server-spans   (->> trace :spans (filter server-span?))
        consumer-spans (->> trace :spans (filter consumer-span?))]
    (map (span->execution-box trace) (concat server-spans consumer-spans))))

(s/defn arrows :- [s-sequence-diagram/Arrow]
  [trace :- s-jaeger/Trace]
  (let [client-spans (->> trace :spans (filter client-span?))]
    (reduce (span->arrow-pair trace) [] client-spans)))
