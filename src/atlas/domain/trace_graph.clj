(ns atlas.domain.trace-graph
  (:require [atlas.schemata.jaeger :as s-jaeger]
            [atlas.schemata.trace-graph :as s-trace-graph]
            [common-clj.misc :as misc]
            [schema.core :as s]))

(defn- find-parent-span [{:keys [references]} span-map]
  (->> references (filter #(= :child-of (:ref-type %))) (map :span-id) first span-map))

(defn- build-span-map [spans]
  (misc/map-vals first (group-by :span-id spans)))

(defn- span->node [{:keys [process-id]} processes]
  {:service (-> processes process-id :service-name)})

(defn- spans->edge [{parent-id :process-id} {child-id :process-id} processes]
  {:from (-> processes parent-id :service-name)
   :to   (-> processes child-id :service-name)})

(s/defn build-graph :- s-trace-graph/Graph
  [{:keys [spans processes]} :- s-jaeger/Trace]
  (let [span-map (build-span-map spans)]
    (reduce (fn [graph span]
              (let [parent-span (find-parent-span span span-map)]
                (cond-> graph
                  true        (update :nodes conj (span->node span processes))
                  parent-span (update :edges conj (spans->edge parent-span span processes)))))
            {:nodes #{}
             :edges #{}}
            spans)))
