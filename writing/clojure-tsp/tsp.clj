(ns algorithms.class4.week2.tsp
  (:require [algorithms.util :refer [read-number-file time-it]]
            [clojure.math.combinatorics :as comb]))

;; The file that contains the data for the actual class assignment:
(def default-file "tsp.txt")

;; For purposes, we need an equivalent of pos-Inf:
(def ^:private +Inf Float/MAX_VALUE)

;; Calculate the Euclidian distance:
(defn- distance [p1 p2]
  (let [[x1 y1] p1
        [x2 y2] p2]
    (Math/sqrt (+ (* (- x1 x2) (- x1 x2))
                  (* (- y1 y2) (- y1 y2))))))

;; Transform a list of number-strings into floats.
(defn- numstrs-to-floats [s] (map #(Float/parseFloat %) s))

;; Create an edge from two vertices:
(defn- edge [u v] (set (list u v)))

;; Create a vector of the sets that are used as the first column indices in
;; the dynamic programming array.
(defn- create-sets [n]
  (let [all-sets (map set (map #(cons 1 %) (comb/subsets (range 2 (inc n)))))
        grouped  (group-by count all-sets)
        sets-vec (vec (repeat (inc n) nil))]
    (reduce (fn [ret x]
              (assoc ret x (grouped x)))
            sets-vec (range 1 (inc n)))))

;; Create the hash-map of the edges and weights/distances.
(defn- create-weights [data]
  (let [n       (first data)
        vdata   (vec data)
        edges   (map set (comb/combinations (range 1 (inc n)) 2))]
    (reduce (fn [ret x]
              (let [p1 (vdata (first x))
                    p2 (vdata (last x))]
                (assoc ret x (distance p1 p2))))
            {} edges)))

;; Create an array column for the given list of sets. Start them with +Inf in
;; slot 1, the rest of the slots nil.
(defn- create-column [sets template]
  (persistent!
   (reduce (fn [ret x]
             (assoc! ret x template))
           (transient {}) sets)))

;; Calculate the final answer by scanning over the solutions for the full set
;; and taking the minimum value (including the last link from j to 1).
(defn- get-final-answer [m-cur n sets weights]
  (let [finals (m-cur (first sets))]
    (loop [j 2, values ()]
      (cond
        (> j n) (apply min values)
        :else   (recur (inc j)
                       (cons (+ (finals j) (weights (edge j 1)))
                             values))))))

;; Technically, *this* is the inner-most loop. But in the algorithm's
;; specification, it's just expressed as a minimum operation over the values
;; of s, excluding j.
(defn- min-val-over-s [m-prev s j weights]
  (let [s'       (disj s j)
        elements (sort s')]
    (loop [[k & es] elements
           values   ()]
      (cond
        (nil? k) (apply min values)
        :else    (recur es (cons (+ (get-in m-prev [s' k])
                                    (weights (edge k j)))
                                 values))))))

;; Run the inner-most loop, the loop of j over the elements of set s, excluding
;; the value 1.
(defn- j-loop [m-prev m-cur s weights]
  (let [elements (rest (sort s))]
    (loop [[j & es] elements, m-cur m-cur]
      (cond
        (nil? j) m-cur
        :else    (recur es
                        (assoc-in m-cur [s j]
                                  (min-val-over-s m-prev s j weights)))))))

;; Run the middle of the three loops, the loop over sets of size m:
(defn- sets-loop [m-prev m-cur sets weights]
  (loop [[s & ss] sets, m-cur m-cur]
    (cond
      (nil? s) m-cur
      :else    (recur ss (j-loop m-prev m-cur s weights)))))

;; The basic entry-point for the TSP dynamic programming algorithm. Sets up the
;; data structures and runs the outer-most loop, the m-loop from 2 to n. At the
;; end of that loop, extracts the final answer from the last map.
(defn- tsp [data]
  (let [n            (first data)
        weights      (create-weights data)
        sets         (create-sets n)
        template-vec (assoc (vec (repeat (inc n) nil)) 1 +Inf)
        m-prev       (hash-map #{1} (assoc (vec (repeat (inc n) nil)) 1 0))]
    (loop [m 2, m-prev m-prev]
      (cond
        (> m n) (get-final-answer m-prev n (sets n) weights)
        :else
        (let [m-cur (create-column (sets m) template-vec)]
          (recur (inc m)
                 (sets-loop m-prev m-cur (sets m) weights)))))))

;; Run the algorithm on the data from the given file.
(defn run-tsp [file]
  (let [data   (read-number-file file false)
        n      (Integer/parseInt (ffirst data))
        points (map numstrs-to-floats (rest data))
        data'  (cons n points)]
    (time-it (tsp data'))))
