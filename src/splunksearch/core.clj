(ns splunksearch.core
  (:require [clojure.java.io :refer :all])
  (:import (com.splunk Service ServiceArgs Args ResultsReaderJson ResultsReaderCsv ResultsReaderXml Event))
  (:import (com.splunk Command))
  (:import (java.io InputStreamReader OutputStreamWriter)))


; Given a set of command-line arguments, creates, populates and
; returns a splunk.Command instance.

(defn build-splunk-command [args]
  (let [command 
        (doto (Command/splunk "search")
          (.addRule "count" Integer 
                    "The Maximum Number of results to return (default: 100)")
          (.addRule "earliest_time" String
                    "Search earliest time")
          (.addRule "field_list" String
                    "A comma-separated list of the fields to return")
          (.addRule "latest_time" String
                    "Search latest time")
          (.addRule "offset" Integer
                    "The first result (inclusive) from which to begin returning data. (default: 0)")
          (.addRule "output" String
                    "Which search results to output {events, results, preview, searchlog, summary, timeline} (default: results)")
          (.addRule "output_mode" String
                    "Search output format {csv, raw, json, xml} (default: xml)")
          (.addRule "reader" 
                    "Use ResultsReader")
          (.addRule "status_buckets" Integer
                    "Number of status buckets to use for search (default: 0)")
          (.addRule "verbose"  
                    "Display search progress")
          (.parse (into-array String args)))]
    (if (not= (count (.args command)) 1)
      (Command/error "Search expression required" nil))
    command))


; Given an initialized splunk.Command instance, returns a map of
; arguments commonly used to control search behavior.  The repetition
; of names above is a giveaway that something is a bit off here.

(defn build-argument-map [command]
  (let [opts (.opts command)
        ruleset [["count" 100] 
                 ["earliest_time" nil ]
                 ["reader" false]
                 ["verbose" false]
                 ["field_list" nil ] 
                 ["latest_time" nil ]
                 ["offset" 0]
                 ["output" "results"]
                 ["output_mode" "xml"]]]
    doall (into {} (for [[k v] ruleset] [k (if (.containsKey opts k) (.get opts k) v)]))))


; From a map of commonly used control arguments, create, populate, and
; return a splunk.Args instance suitable for passing to a query

(defn build-splunk-queryargs [argument-map]
  (let [rulelist ["earliest_time" "field_list" "latest_time" "status_buckets" "output_mode"]
        qa (Args.)]
    (filter 
     (fn [a] (not= a nil))
     (for [fieldname rulelist]
       (fn [fieldname] 
         (if (argument-map fieldname) 
           (.put qa fieldname (argument-map fieldname)) 
           nil))))
    qa))


; Given a splunk.Command instance and a set of initialized arguments,
; generate an actual query; wait until the query is done.

(defn build-splunk-job [command argument-map] 
  (let [queryargs (build-splunk-queryargs argument-map)
        service (Service/connect (.opts command))
        job (.. service (getJobs) (create (first (.args command)) queryargs))]
    (while (not (.isDone job)) 
      (if (argument-map "verbose")
        (println (format "\n%03.1f%% done -- %d scanned -- %d matched -- %d results"
                         (* (.getDoneProgress job) 100.0)
                         (.getScanCount job) 
                         (.getEventCount job) 
                         (.getResultCount job))))
      (Thread/sleep 1000))
    job))

; From a map of commonly used control arguments, create, populate, and
; return a splunk.Args instance suitable for passing to a splunk.Job
; specifying desired output paramaters.

(defn build-splunk-output-args [argument-map]
  (let [rulelist ["count" "offset" "output_mode"]
        args (Args.)]
    (doseq [fieldname rulelist]
      (if (argument-map fieldname)
        (.put args fieldname (argument-map fieldname))))
    args))

; Given a job and a map of commonly use arguments, return an
; InputStream with the content of the response

(defn get-splunk-stream [job argument-map]
  (let [outputargs (build-splunk-output-args argument-map)
        output (argument-map "output")]
    (case output
      "results" (.getResults job outputargs)
      "preview" (.getPreview job outputargs)
      "searchlog" (.getSearchLog job outputargs)
      "summary" (.getSummary job outputargs)
      "timeline" (.getTimeline job outputargs)
      )))

(defn construct-reader [stream output-mode]
  (case output-mode
    "xml" (ResultsReaderXml. stream)
    "json" (ResultsReaderJson. stream)
    "csv" (ResultsReaderCsv. stream)))

(defn get-streamtype-reader [output-mode]
  (fn [stream] 
    (with-open [reader (construct-reader stream output-mode)]
      (loop [e (.getNextEvent reader)]
        (when (not= e nil)
          (println "EVENT:********")
          (doseq [k (seq (.keySet e))]
            (printf "%s ---> %s\n" k (.get e k)))
          (recur (.getNextEvent reader)))))))

; A totally unnecessary level of indirection, but it maintains an
; aesthetic symmetry.

(defn generic-reader []
  (fn [stream]
    (with-open [reader (InputStreamReader. stream "UTF8")
                writer (OutputStreamWriter. System/out)]
      (try
        (let [buffer (char-array 1024)]
          (while true 
            (let [count (.read reader buffer)]
              (if (== count -1) (throw (Exception. "EOF")))
              (.write writer buffer 0 count))))
        (catch Exception e nil)))))

(defn get-reader [command argument-map]
  (let [use-reader (.. command opts (containsKey "reader"))]
    (if use-reader
      (get-streamtype-reader (argument-map "output_mode"))
      (generic-reader))))

(defn -main[& args]
  (let [command (build-splunk-command args)]
    (let [argument-map (build-argument-map command)]
      (let [job (build-splunk-job command argument-map)]
        (let [stream (get-splunk-stream job argument-map)]
          (let [reader (get-reader command argument-map)]
            (reader stream)))))))

