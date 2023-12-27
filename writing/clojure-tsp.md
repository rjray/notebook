# Clojure, Advent of Code, and the Traveling Salesman

Date: `Tue Dec 26 21:53:05 CST 2023`

Tags: `#clojure` `#adventofcode` `#aoc` `#algorithms` `#tsp`

## Introduction

I thoroughly enjoy taking part in the yearly [Advent of
Code](https://adventofcode.com/) programming challenge. My first year was 2018,
and I've done it each year since (except for 2021, when I was steeped in exams
for graduate classes in my MSCS program). I use AoC to practice my skills in
the [Clojure](http://clojure.org/) programming language (a Lisp dialect written
in Java and running on the JVM).

The 2023 challenge will be done and in the books by the time I publish this. I
generally have a common problem early on in each year's effort: I don't get to
use Clojure throughout the year, so each December I feel like I'm practically
starting over. I spend the first few days madly going through the
[documentation](https://clojuredocs.org/) to re-learn what I had been
comfortable with a year earlier. And even into the later days, I'm still
reaching for docs far more often than I should be. It's frustrating, of course,
and it slows me down (especially on the early days that should be the easiest).

With this in mind, I decided to do a "practice run" this year prior to the
start of the new challenge. I chose to do 2015 (the first year) as fast as I
could. The goals I set for this were:

* Refresh my understanding and familiarity with Clojure
* Tune the [template-project](https://github.com/rjray/advent-clojure-basis) I
  use, to make it more efficient and usable
* Look for any new snippets, etc. that I can add to my "toolkit" for use in
  future challenges

So everything was steaming along nicely in my run of 2015... until I hit Day 9.

## The 2015 Day 9 Problem

The [day 9 puzzle](https://adventofcode.com/2015/day/9) for 2015 was to take a
set of towns, the distances between each pair, and find the shortest path that
will visit all towns. Sound familiar? It's better known as the ["Traveling
Salesman Problem"](https://en.wikipedia.org/wiki/Travelling_salesman_problem).
This made me enthusiastic, as I had written Clojure code for solving the TSP
many years ago when I was working through the 4-course [Algorithms
Specialization](https://www.coursera.org/specializations/algorithms) from
Coursera. I had completed all 4 courses while writing all the programming
assignments in Clojure, and I still had the code!

I pulled out the [TSP solution I had originally written](clojure-tsp/tsp.clj)
and began to examine it to remember what I had done. This implementation used
a dynamic programming approach with a format based on the [Bellman-Ford
algorithm](https://en.wikipedia.org/wiki/Bellman%E2%80%93Ford_algorithm).

Remembering what I did that far back was it's own challenge, of course. There
are some comments in that code, but never enough. After a bit, though, I had
determined what I needed to do in order to adapt the code to the input for the
AoC puzzle. After some adjustments, I ran it on the example given in the puzzle
and go the correct answer. I then ran it on the puzzle input and looked at the
result; it felt too low. There were 8 towns in the dataset, and it seemed like
the number should just be higher than what I had. I spent time running the
algorithm by hand with pencil and paper, and decided that it must be the
correct answer.

It wasn't the correct answer. In fact, it was *too high.*

## Explanation of the Algorithm

This situation was a simpler one than the typical application of Bellman-Ford:
there are no negative edges, the graph is fully-connected, and the edges are
not directed (the weight of edge $(A, B)$ is the same as that of $`(B, A)`$).
These conditions also applied to the assignment problem in the Coursera class.

The basic gist of the algorithm, as applied to TSP, is:

1. Choose a vertex $v$ as the starting point. In the original code, vertices
   are numbered from 1 to $n$, so for the sake of easier looping the first
   vertex (1) is chosen.
2. Use the Bellman-Ford-inspired DP approach to find the shortest paths rooted
   at $v$. There will be $n-1$ different candidate paths.
3. Over the paths from the previous step, find the shortest path that includes
   a return segment back to $v$.

Here is the basic pseudo-code (using bullet-points for indentation so I can
still use math symbols):

Let $A$ be a 2-dimensional array, indexed by subsets $S \subseteq \{1..n\}$ that
contain 1 and destinations $j \in \{1..n\}$

* (Base case) $A[S,1] = \bigl\\{ 0\~if\~S = \{1\},\~+\infty\~otherwise \bigr\\}$
* for $m$ = 2..$`n`$:
  * for each set $S \subseteq \{1 .. n\}$ of size $m$ that contains 1:
    * for each $j \in S,\~j \ne 1$:
      * $A[S,j] = \min_{k \in S, k \ne j} \bigl\\{ A[S-\{j\},k] + C_{kj} \bigr\\}$
    * end
  * end
* end
* Return $\min_{j = 2..n} \bigl\\{ A[\{1,2,3,..,n\},j] + C_{j1} \bigr\\}$

In this case, it should have been enough to take the lowest-score (shortest)
path while skipping the "return segment back to $v$" step (since a return
segment was not part of the puzzle). I had done this, but it had given the
wrong answer.

## Back to the Advent Problem: Going Brute Force

Returning to the specific problem, I spent the better part of the spare time I
had for 1-2 days trying to determine what was wrong. I simply could not find
out what the underlying problem was. I became frustrated and annoyed, so I
consulted the [reddit
thread](https://www.reddit.com/r/adventofcode/comments/3w192e/day_9_solutions/)
for that year/day. It seemed that most people were just taking a brute-force
approach to it, so I decided to [follow suit](clojure-tsp/day09.clj).

To tackle this, I used the
[clojure.math.combinatorics](https://github.com/clojure/math.combinatorics)
package, specifically the `permutations` function:

```clojure
(defn- calc-cost [p edges]
  (apply + (map edges (map set (partition 2 1 p)))))

(defn- brute-force [fun ival [n edges]]
  (reduce fun ival (map #(calc-cost % edges)
                        (comb/permutations (range 1 (inc n))))))
```

Looking at `brute-force` first, `fun` is a function to apply (one of `min` or
`max`) because part 2 of the puzzle was to find the longest path. The `ival`
parameter is the initial value to use with `reduce` (again, because of the
min/max nature of the two parts). The two destructured parameters, `[n edges]`,
represent the number of vertices and the map containing the edge-weights for
each possible pair. Since this code was derived from the previous TSP attempt,
I had left the vertices numbered from 1 rather than 0.

The `calc-cost` function is used to calculate the total cost for a permutation.
It does this by first partitioning the list of vertices into overlapping pairs.
These are then used to key into the `edges` map to get the weight.

This solved the puzzle, giving me correct answers for both parts. The run-time
on my 2015-era MacBook Pro was 780.277ms for part 1 and 790.876ms for part 2.
But I was absolutely certain that I could do better, and equally certain that
the path to "better" was through the dynamic programming TSP solution.

(As a side note: the run-times could have possibly been reduced if I took the
time and effort to recognize that two permutations that are exact reverses of
each other would have the same cost. This would have required caching each
permutation and not calculating a cost for any permutation in which `(reverse
p)` was already in the cache. But the `calc-cost` function was not the biggest
source of processing time, the calculation and iteration of $8!$ permutations
was. Add in the overhead of maintaining the cache and testing each element for
presence, and it probably would not have saved any significant amount of time.)

## Now, Back to the TSP Code

It really bothered me that I hadn't been able to get the TSP code to work. I
was also convinced that the DP approach would significantly out-strip the
brute-force version. I returned to trying to determine the root of my problem
with the TSP code. This time, however, I had a key piece of additional
information: the correct answer.

Using a variety of debugging approaches, both interjected `(prn ...)`
statements and the [CIDER](https://cider.mx/) step-wise debugger, I walked
through the code time and time again. After I-don't-know-how-many iterations,
something hit me: the code was correctly producing the $n-1$ candidate paths
(starting at vertex 1) and correctly picking the shortest of them (without
adding a link back to the starting vertex)... but what if the correct solution
*doesn't* start at 1?

### The problem

The original application of Bellman-Ford to this connected the path back to
vertex 1 at the end. This is because for the TSP problem we are looking for a
closed loop, we end where we started. This works, because the final answer
works for any rotation of the vertices as long as the order is maintained. In
other words, `(1 2 3 4)` and and `(3 4 1 2)` would yield the same cost (once
the closing link is added). To illustrate, take the two examples and append the
starting-point onto the list, yielding `(1 2 3 4 1)` and `(3 4 1 2 3)`. When
the overlapping application of `partition` is done, we have the two following
sequences of pairs:

```clojure
(1 2 3 4 1) => ((1 2) (2 3) (3 4) (4 1))
```

and

```clojure
(3 4 1 2 3) => ((3 4) (4 1) (1 2) (2 3))
```

Clearly, these are the same 4 pairs and will result in the same sum of
edge-costs.

But this puzzle *doesn't close the loop*. Without that, `(1 2 3 4)` and `(3 4 1
2)` do **not** yield the same costs. I can no longer arbitrarily start at 1 and
expect it to give the correct answer.

### The solution

To address this, the only approach I could think of was to iterate over the DP
algorithm for each vertex as a starting point. I would then take the minimum
(or maximum, for part 2) resulting cost. This sounds very brute-force-ish, I
admit. But I suspected that even running the full DP process $n$ times should
still out-perform the original solution.

This lead to the file [day09bis.clj](clojure-tsp/day09bis.clj). The hardest
part of this was re-factoring the primary steps and loops to deal with the
fixed vertex being something other than 0. (At this point I had also dropped
the practice of numbering from 1 rather than 0.) In this file, the functions
are parameterized to allow for choosing between `min` and `max` for the
selection operation (and `Integer/MAX_VALUE` versus `Integer/MIN_VALUE` for
positive/negative infinty values).

Running this yielded running times of 83.789ms for part 1 and 75.497ms for
part 2. These times are roughly 10% of the corresponding brute-force times.

## But How Does the Code *Work?*

Let's look at the `day09bis.clj`
(["bis"](https://www.merriam-webster.com/dictionary/bis)) file, more or less
line-by-line. There are some large-ish block-comments in this file (in case I
need to understand a working TSP implementation in the future) that I'll skip
over. Not all variable names will be clear, so I'll try to explain them as I
go.

(Disclaimer: I am not an expert Clojure programmer. There are likely several
places where something could have been done more concisely, or in a more
idiomatic manner. Also, I write for clarity over terseness, on the assumption
that I'll want to go back and read the code at some future point.)

### Basic algorithm

The basic algorithm here is [Dynamic
Programming](https://en.wikipedia.org/wiki/Dynamic_programming). In this case,
the DP approach would create a matrix where the rows run from 0 to $n$
(inclusive), where $n$ is the number of vertices. The number of columns would
generally be the number of distinct subsets of $S$, where $S$ is the set of
numbers $\lbrace0, ..., n-1\rbrace$. However, two optimizations are done to
reduce memory usage:

1. Not all possible subsets of $S$ are used, only those that contain the given
   "anchor" vertex
2. Not all columns of the matrix are allocated. For a given row $m$, only the
   columns whose sets have $m$ elements are ever allocated.

At each step of the outer-most loop (not counting the meta-loop for running the
DP code for one specific anchor vertex), the previous row is used to calculate
the new (current) row. The previous row is discarded, and the new row becomes
the new "previous" for the next iteration.

At the end, the final row is left with just one column allocated, the column
for which the set-index is $S$. That column will have the total cost values
based on each non-anchor element, and the aggregate function will be applied to
these.

### Preamble

The code starts with these four lines:

```clojure
(ns advent-of-code.day09bis
  (:require [advent-of-code.utils :as u]
            [clojure.string :as str]
            [clojure.math.combinatorics :as comb]))
```

These are basic boilerplate lines. The namespace is declared, followed by a
`require` of three different packages:

* `advent-of-code.utils` is my grab-bag of utility code for AoC
* `clojure.string` provides a string-utility that will be used here (`split`)
* `clojure.math.combinatorics` is a third-party library that provides a range
  of mathematical/combinatorics primitives

The combinatorics library is one that gets a lot of use in every year of AoC.

### Setting up the data

The first two functions are used for parsing the data and converting it to a
representation of a graph:

```clojure
(defn- parse-line [line]
  (let [[src _ dst _ dist] (str/split line #"\s+")]
    (list src dst (parse-long dist))))

(defn- build-graph [tuples]
  (let [cities-list (sort (distinct (apply concat
                                           (map #(list (first %) (second %))
                                                tuples))))
        cities-map  (into {} (map hash-map cities-list (range)))]
    (loop [[[s d ds] & tuples] tuples, edges {}]
      (cond
        (nil? s) {:edges edges, :cities cities-map}
        :else    (recur tuples
                        (assoc edges #{(cities-map s) (cities-map d)} ds))))))
```

In the first function, `parse-line`, a single line of input is turned into a
tuple of *(city1, city2, distance)*. There's nothing really noteworthy here;
the structure of the input lines meant that `clojure.string/split` could be
used in this case to get everything needed out of a line.

The `build-graph` function takes the sequence of tuples produced by the
previous function and turns them into a graph structure. The code first takes
all of the tuples and extracts a list of distinct city-names from it. Then this
group is used to convert the names to numeric indices as each pair's cost-value
is recorded as an edge.

The conversion of the cities from names to numbers was needed so that the key
loops of the algorithm could operate on numbers rather than repeatedly
iterating over a list of names. However, saving the set of city names wasn't
necessary. Also, the `loop` construct to turn the tuples into a set of edges
could very likely be written more succinctly and in a more Clojure-ish way.

### Utility functions

The next four functions are not really part of the algorithm, so they'll be
covered together.

```clojure
(defn- n-sans-m [n m]
  (filter #(not= m %) (range n)))

(defn- create-sets [n m]
  (let [all-sets (map set (map (partial cons m) (comb/subsets (n-sans-m n m))))
        grouped  (group-by count all-sets)
        sets-vec (vec (repeat n nil))]
    (reduce (fn [ret x]
              (assoc ret x (grouped (inc x))))
            sets-vec (range n))))

(defn- create-column [sets template]
  (persistent!
   (reduce (fn [ret x]
             (assoc! ret x template))
           (transient {}) sets)))

(defn- get-final-answer [f m-cur sets]
  (let [finals (m-cur (first sets))]
    (apply f (filter (comp not nil?) finals))))
```

Starting with `n-sans-m`, we have a simple function that returns the list of
numbers from 0 to $n-1$, with the number $m$ removed. This is important because
we'll be running the DP algorithm $n$ times, each time with a different
starting vertex $m$.

The `create-sets` function is a subtle part of what makes Clojure such a
well-suited language for this problem. Here, we create all the subsets of $S$
($S = \lbrace0, ..., n-1\rbrace$), but we only create the ones that contain
$m$. This is done by creating all sets without $m$, then `cons`'ing $m$ into
all of these. The resulting sequence of sets is then grouped by the count of
elements. The `reduce` block converts that result into a vector such that each
index $i$ points to all sets that have $i+1$ elements, accounting for $m$.

Next, `create-column` creates a single column for the matrix that would be used
for the DP approach. It takes `sets` and `template`, and creates a
pseudo-vector of all the sets (as indices) pointing to copies of `template`.
The purpose of this will be explained in more detail later. Note that this is
actually a map structure rather than a vector, so that a set can be used as the
index.

Lastly, `get-final-answer` finds the correct answer (for the iteration of TSP).
Here, `f` is the aggregate function to be applied to the collected totals.
`m-cur` is the final column-function from the DP algorithm (more on that later)
and `sets` is the one-element list of "subsets" of $S$ that is just $S$.

### The loop-control functions

These four functions represent the different loops that make up the DP
algorithm for solving TSP. Because Clojure requires functions to be defined
before being referenced, they are listed from inner-most to outer-most but will
be explained in reverse order.

```clojure
(defn- f-val-over-s [f m-prev s j weights]
  (let [s'       (disj s j)
        elements (sort s')]
    (loop [[k & ks] elements, values ()]
      (cond
        (nil? k) (apply f values)
        :else    (recur ks (cons (+ (get-in m-prev [s' k])
                                    (weights #{k j}))
                                 values))))))

(defn- j-loop [f st m-prev m-cur s weights]
  (let [js (sort (disj s st))]
    (loop [[j & js] js, m-cur m-cur]
      (cond
        (nil? j) m-cur
        :else    (recur js
                        (assoc-in m-cur [s j]
                                  (f-val-over-s f m-prev s j weights)))))))

(defn- sets-loop [f st m-prev m-cur sets weights]
  (loop [[s & ss] sets, m-cur m-cur]
    (cond
      (nil? s) m-cur
      :else    (recur ss (j-loop f st m-prev m-cur s weights)))))

(defn- tsp-core [n weights f s start]
  (let [template (assoc (vec (repeat n nil)) start s)
        sets     (create-sets n start)
        m-prev   (hash-map #{start} (assoc (vec (repeat n nil)) start 0))]
    (loop [[m & ms] (range 1 (inc n)), m-prev m-prev]
      (cond
        (nil? ms) (get-final-answer f m-prev (sets (dec n)))
        :else
        (let [m-cur (create-column (sets m) template)]
          (recur ms (sets-loop f start m-prev m-cur (sets m) weights)))))))
```

Starting with `tsp-core`, this is the "main" loop of the algorithm. This runs
the basic DP/Bellman-Ford algorithm with `start` as the anchored vertex. The
`n` and `weights` variables are the number of vertices and the edge-weights,
respectively. `f` is the aggregate function and `s` is the outlier value that
is used in the template creation to represent one of $\pm\infty$. It creates
`template` as a vector of `nil` values with `s` in the `start` slot, `sets` as
the indexed collection of subsets, and `m-prev` as the initial "previous" row
of the DP matrix. The `loop` construct runs `m` from 1 to $n$ (inclusive) and
carries over `m-prev` between iterations. This is a place where I could have
save some keystrokes: The `let` is unnecessary in that the s-expression it
binds could be plugged directly into the `recur` expression in place of
`m-cur`. Within the `loop`, calls are made to the next function, `sets-loop`.

The `sets-loop` function performs what is the middle of the three loops
described by the DP algorithm. It takes the aggregate function `f`, the
starting vertex `st`, the "previous" row of the matrix `m-prev`, the
"current" matrix row (that is being filled in) `m-cur`, the list of subsets
of $S$ based on the value of $m$ in the previous function's loop, and finally
the edge weights. With all of this, it just `loop`'s over the list of sets,
calling the `j-loop` function for each individual set in the list.

In the textbook algorithm, `j-loop` is the innermost loop. It loops over the
elements of the set `s` that *aren't* the start-vertex `st`. For each of these,
the `m-cur` row is updated for set-index `s` and integer index `j`. The new
value is the result of calling `f-val-over-s` with the group of parameters.

`f-val-over-s` is the *actual* innermost loop. In the textbook illustration of
the algorithm it is simply listed as a "min" operation over a function
performed on all elements of the set (minus the `j` value). This is done by
using `disj` to take `j` out of `s` (creating `s'`) and iterating over those
elements (again, assigning `elements` was for readability and could have been
skipped, as could the sorting of the elements of `s'`). A list of values is
created based on the edge-weight for $(j, k)$ and the value in the previous row
for `s` and `k`. The aggregate function `f` is applied to the final list of
values, and this value is returned. In the original TSP code, this function was
valled `min-val-over-s` since the operation was always `min`. Here it is
parameterized as `f`.

### The (small) `tsp` function

In the original TSP code, this function was essentially what is now `tsp-core`.
Here, it makes $n$ calls to `tsp-core`.

```clojure
(defn- tsp [f s graph]
  (let [n       (count (:cities graph))
        weights (:edges graph)]
    (apply f (map (partial tsp-core n weights f s) (range n)))))
```

The key element here is the use of `partial` to construct a partial application
of `tsp-core` with `n`, `weights`, `f` and `s` locked in. This partial will be
called with each value from 0 to $n-1$ via `map`, and the "best" value (either
minimum or maximum) will be pulled out through application of the aggregate
function `f`.

### Running the parts

Running the above parts is done through the pre-defined functions `part-1` and
`part-2`. These are part of the
[framework](https://github.com/rjray/advent-clojure-basis) I use for AoC.

```clojure
(defn part-1
  "Day 09 Part 1"
  [input]
  (->> input
       u/to-lines
       (map parse-line)
       build-graph
       (tsp min Integer/MAX_VALUE)))

(defn part-2
  "Day 09 Part 2"
  [input]
  (->> input
       u/to-lines
       (map parse-line)
       build-graph
       (tsp max Integer/MIN_VALUE)))
```

These are identical aside from their very last s-expressions. For part 1 we
want the minimum-cost path; the aggregate function is `min` and the $+\infty$
value is `Integer/MAX_VALUE`. For part 2, we want the maximum-cost path. So the
aggregate is `max` and the $-\infty$ value is `Integer/MIN_VALUE`.

## Final Thoughts

Using the [sloc](https://github.com/flosse/sloc) tool, I looked at the basic
size of the solutions: 41 lines for the brute-force solution and 87 lines for
the DP/Bellman-Ford solution. The latter could be squeezed a bit at the expense
of readability, but I am not motivated by code-golfing.

What matters, is that even running the DP algorithm $n$ times, for a value of
$n=8$ it took only 10% of the time that the brute-force algorithm did. As $n$
grows, this gap will become much more pronounced, as the brute force will be
bounded by O($n!$) while the DP code will be bounded by O($n^4$) (when
accounting for the additional loop over $n$ for testing all vertices). The
cross-over where DP becomes efficient occurs at $n=7$.

I am a little "itchy" about the number of parameters passed down from loop to
loop, between `tsp` and `f-val-over-s`. There may be better ways to do this,
with either `letfn`, `partial`, or lexical bindings. More and better
familiarity with Clojure will be key, here.

## Epilogue: Post-AoC 2023

Though I started the first draft of this well prior to the start of the 2023
Advent of Code, I'm putting the final polish on it just after Christmas Day,
after completing the 2023 series of puzzles.

My code from the Coursera series came into play this year, as well. For the Day
25 challenge, we needed to compute a [minimum
cut](https://en.wikipedia.org/wiki/Minimum_cut) of a graph. For this, I reached
back into the Coursera code, where I had implemented [Karger's
algorithm](https://en.wikipedia.org/wiki/Karger%27s_algorithm). But I learned a
harsh lesson: even generous code-comments can be of limited helpfulness if they
are too general. It took me several hours to recall the structure of the
algorithm and how the implementation worked. Once I had back-engineered the
original work, it produced the correct answer (given enough iterations). But
most of all, I found that most days went faster than they usually do (though
there were still days that took a significant length of time). Interestingly, I
*also* submitted more incorrect initial answers than previous years. But
overall I would say that the practice-run was worth the time and effort.
