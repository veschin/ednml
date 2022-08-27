(ns core.watch
  (:gen-class)
  (:require [hawk.core :as hawk]
            [clojure.edn]
            [clojure.string :as string]
            [core.ednml]))

(defn notify-user! [message]
  (with-out-str (println message)))

(defn make-html-file [ipath opath]
  (let [input        (core.ednml/try!
                      (clojure.edn/read-string (slurp ipath))
                      (notify-user! "Wrong input file path\n"))
        html-content (core.ednml/->html input)]
    (core.ednml/try!
     (println "Compiled in"
              (string/replace
               (with-out-str (time (spit opath html-content)))
               #"Elapsed time\:|\"" ""))
      (notify-user! "Wrong output file path"))
    :done))

(defn watch [ipath opath]
  (def watcher
    (hawk/watch!
     [{:paths [ipath]
       :handler (constantly (make-html-file ipath opath))}])) )

(defn help []
  (str
   "\nCompile - complie once:\n \t -c -i path/read.edn -o path/write.html\n"
   "\nWatch - recompile on save:\n \t -w -i path/read.edn -o path/write.html\n") )

(defn -main [& args]
  (if (not-empty args)
    (let [options  #{"-c" "-w"}
          commands #{"-c" "-w" "-h"}
          both?    (= 2 (count (filter options args)))]
      (cond
        (not (commands (first args)))
        (println "Wrong command -> " (first args))

        both?
        (println "U can't use a watch and compile at the same time.")

        (> (count args) 5)
        (println "Wrong number of arguments")

        (apply #{"-c" "-w" "-h"} args)
        (let [[cmd _ ipath _ opath] args]
          (case cmd
            "-h"
            (println (help))

            "-c"
            (make-html-file ipath opath)

            "-w"
            (watch ipath opath)))))
    (println "No args")))

(comment

  (-main "-h")

)
