(ns atlas.domain.sequence-diagram
  (:require [atlas.schemata.jaeger :as s-jaeger]
            [atlas.schemata.sequence-diagram :as s-sequence-diagram]
            [common-clj.schema.core :as cs]
            [java-time :as time]
            [schema.core :as s]))

(defn- microseconds->epoch [microseconds] (time/instant (/ microseconds 1000)))

(defn- span->end-time [{:keys [start-time duration]}]
  (let [start-epoch (microseconds->epoch start-time)]
    (time/plus start-epoch (time/micros duration))))

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

(defn- child-of? [{:keys [span-id]}]
  (fn [{:keys [references]}]
    (->> references (filter #(= span-id (:span-id %))) first boolean)))

(defn- parent-of? [{:keys [references]}]
  (fn [{:keys [span-id]}]
    (->> references (filter #(= span-id (:span-id %))) first boolean)))

(defn- span->service-name [{:keys [process-id]} trace]
  (-> trace :processes process-id :service-name))

(defn- span->execution-box [trace]
  (fn [{:keys [span-id start-time duration] :as span}]
    {:id          span-id
     :start-time  (microseconds->epoch start-time)
     :duration-ms (/ duration 1000)
     :lifeline    (span->service-name span trace)}))

(defn span->log-type [span]
  (cond
    (server-span? span)
    :in-request

    (consumer-span? span)
    :in-message

    (client-span? span)
    :out-request

    (producer-span? span)
    :out-message))

(defn- span->node [trace]
  (fn [{:keys [span-id start-time duration] :as span}]
    {:id       (str "service-" span-id)
     :time     (microseconds->epoch start-time)
     :meta     {:log       (name (span->log-type span))
                :log-level "INFO"
                :time      (str (microseconds->epoch start-time))}
     :lifeline (span->service-name span trace)}))

(defn- server-span->in-response-node [trace]
  (fn [{:keys [span-id start-time duration] :as span}]
    {:id       (str "service-" span-id "-in-response")
     :time     (span->end-time span)
     :meta     {:log       "in-response"
                :log-level "INFO"
                :time      (str (span->end-time span))}
     :lifeline (span->service-name span trace)}))

(defn- client-span->out-response-node [trace]
  (fn [{:keys [span-id start-time duration] :as span}]
    {:id       (str "service-" span-id "-out-response")
     :time     (span->end-time span)
     :meta     {:log       "out-response"
                :log-level "INFO"
                :time      (str (span->end-time span))}
     :lifeline (span->service-name span trace)}))

(defn tap [x]
  (prn x)
  x)

(defn- span->topic [{:keys [tags]}] (->> tags (find-tag "message_bus.destination") :value))

(defn- producer-span->topic-node [trace]
  (fn [{:keys [span-id start-time duration] :as span}]
    {:id       (str "topic-" span-id)
     :time     (microseconds->epoch start-time)
     :meta     {:log       "out-message"
                :log-level "INFO"
                :time      (str (microseconds->epoch start-time))}
     :lifeline (span->topic span)}))

(defn- ->topic-execution-boxes [spans]
  (let [spans-by-topic (->> spans
                            (filter (some-fn consumer-span? producer-span?))
                            (group-by span->topic))]
    (map
     (fn [[topic spans]]
       (let [start-time (->> spans (sort-by :start-time) first :start-time)
             end-time   (->> spans (sort-by :start-time) last :start-time)
             duration   (- end-time start-time)]
         {:id          (str topic start-time)
          :start-time  (microseconds->epoch start-time)
          :duration-ms (/ duration 1000)
          :lifeline    topic}))
     spans-by-topic)))

(defn- find-child [span trace] (->> trace :spans (filter (child-of? span)) first))

(defn- find-parent [span trace] (->> trace :spans (filter (parent-of? span)) first))

(defn- span->http-method [{:keys [tags]}] (->> tags (find-tag "http.method") :value))

(defn- span->http-url [{:keys [tags]}] (->> tags (find-tag "http.url") :value))

(defn- map-vals [f m]
  (->> m
       (map (fn [[k v]] [k (f v)]))
       (into {})))

(defn- ->service-lifelines [spans trace]
  (->> spans
       (map (fn [span] {:id    (span->service-name span trace)
                        :label (span->service-name span trace)
                        :time  (-> span :start-time microseconds->epoch)
                        :kind  :service}))
       (group-by :id)
       (map-vals (comp first #(sort-by :time %)))
       vals))

(defn- ->topic-lifelines [spans]
  (->> spans
       (filter producer-span?)
       (map (fn [span]
              {:id    (span->topic span)
               :label (span->topic span)
               :kind  :topic
               :time  (-> span :start-time microseconds->epoch)}))
       (group-by :id)
       (map-vals (comp first #(sort-by :time %)))
       vals))

(defn- client-span->arrow [trace]
  (fn [client-span]
    (when-let [server-span (find-child client-span trace)]
      {:id   (:span-id client-span)
       :from ((span->node trace) client-span)
       :to   ((span->node trace) server-span)
       :kind "sync"})))

(defn- server-span->arrow [trace]
  (fn [server-span]
    (when-let [client-span (find-parent server-span trace)]
      {:id   (:span-id server-span)
       :from ((server-span->in-response-node trace) server-span)
       :to   ((client-span->out-response-node trace) client-span)
       :kind "sync"})))

(defn- producer-span->arrow [trace]
  (fn [producer-span]
    {:id   (:span-id producer-span)
     :from ((span->node trace) producer-span)
     :to   ((producer-span->topic-node trace) producer-span)
     :kind "async"}))

(defn- consumer-span->arrow [trace]
  (fn [consumer-span]
    (when-let [producer-span (find-parent consumer-span trace)]
      {:id   (:span-id consumer-span)
       :from ((producer-span->topic-node trace) producer-span)
       :to   ((span->node trace) consumer-span)
       :kind "async"})))

(defn- build-arrows [{:keys [spans] :as trace}]
  (fn [[matcher mapper]] (->> spans (filter matcher) (map (mapper trace)) (remove nil?))))

(s/defn start-time :- cs/EpochMillis
  [trace :- s-jaeger/Trace]
  (->> trace :spans (map :start-time) sort first microseconds->epoch))

(s/defn duration-ms :- cs/PosInt
  [trace :- s-jaeger/Trace]
  (let [start-time (start-time trace)
        end-time   (->> trace :spans (map span->end-time) sort last)]
    (.toMillis (time/duration start-time end-time))))

(s/defn lifelines :- [s-sequence-diagram/Lifeline]
  [{:keys [spans] :as trace} :- s-jaeger/Trace]
  (let [services (->service-lifelines spans trace)
        topics   (->topic-lifelines spans)]
    (->> services (concat topics)
         (sort-by :time)
         distinct)))

(s/defn execution-boxes :- [s-sequence-diagram/ExecutionBox]
  [trace :- s-jaeger/Trace]
  (let [server-spans          (->> trace :spans (filter server-span?))
        consumer-spans        (->> trace :spans (filter consumer-span?))
        topic-execution-boxes (->topic-execution-boxes (:spans trace))]
    (->> consumer-spans
         (concat server-spans)
         (map (span->execution-box trace))
         (concat topic-execution-boxes))))

(s/defn nodes :- [s-sequence-diagram/Node]
  [trace :- s-jaeger/Trace]
  (let [server-spans       (->> trace :spans (filter server-span?))
        client-spans       (->> trace :spans (filter client-span?))
        consumer-spans     (->> trace :spans (filter consumer-span?))
        producer-spans     (->> trace :spans (filter producer-span?))
        in-response-nodes  (map (server-span->in-response-node trace) server-spans)
        out-response-nodes (map (client-span->out-response-node trace) client-spans)
        topic-nodes        (map (producer-span->topic-node trace) producer-spans)]
    (->> [server-spans client-spans consumer-spans producer-spans]
         (apply concat)
         (map (span->node trace))
         (concat in-response-nodes out-response-nodes topic-nodes))))

(s/defn arrows :- [s-sequence-diagram/Arrow]
  [trace :- s-jaeger/Trace]
  (->> [[client-span? client-span->arrow]
        [server-span? server-span->arrow]
        [producer-span? producer-span->arrow]
        [consumer-span? consumer-span->arrow]]
       (map (build-arrows trace))
       (apply concat)))
