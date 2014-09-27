(defproject splunksearch "0.1.0"
  :description "SplunkSearch: An implementation of the Splunk Search example program in Clojure"
  :url "https://github.com/elfsternberg/splunk-search"
  :license {:name "Apache Public Licence 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :repositories [["splunk-release" "http://splunk.artifactoryonline.com/splunk/ext-releases-local"]]
  :dependencies [
                 [org.clojure/clojure "1.5.0"]
                 [commons-cli/commons-cli "1.2"]
                 [cheshire "5.3.1"]
                 [net.sf.opencsv/opencsv "2.3"]
                 [com.splunk/splunk "1.3.1.0"]]
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :main splunksearch.core)
