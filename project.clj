(defproject splunksearch "0.1.0"
  :description "SplunkSearch: An implementation of the Splunk Search example program in Clojure"
  :url "https://github.com/elfsternberg/splunk-search"
  :license {:name "Apache Public Licence 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [
                 [org.clojure/clojure "1.4.0"] 
                 [commons-cli "1.2"] 
                 [gson "2.1"] 
                 [opencsv "2.3"]
                 [splunk "1.1"]]
  :plugins [[lein-localrepo "0.4.1"]]
  :main splunksearch.core)
  
