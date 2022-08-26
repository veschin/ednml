(ns core.ednml
  (:require [clojure.string :as string]
            [css]))

(def ->str #(str "\"" % "\""))

(defn ->opts [opts]
  (if (empty? opts)
    ""
    (string/join
    " "
    (for [[key val] opts
          :let [key (name key)]]
      (->> [key (cond
                  (= "style" key)
                  (->str (string/join " " (map css/prop<->value val)))

                  (map? val)
                  (->str (->opts val))

                  (string? val)
                  (->str val)

                  (number? val)
                  (->str val)

                  (keyword? val)
                  (->str (name val))

                  (boolean? val)
                  nil)]
           (filter some?)
           (string/join "="))))))

(defn preprocess-tag [tag]
  (let [tag-str (str tag ".")
        tag (re-find #":\w+" tag-str)
        id (re-find #"\#.*?[\.|\#]" tag-str)
        classes (string/split "\." (re-seq #"\..*[\.|\#]" tag-str))]
    [tag id classes])
  )

(preprocess-tag :div#id-example.class-example.multiple-class)

(defn ->tag
  [tag & children]
  (if (= :style tag)
    (css/->style (first children))
    (let [opts? (-> children first map?)
          opts  (when opts? (->opts (first children)))
          children- (string/join
                     "\n"
                     (cond
                       (or (empty? children)
                           (empty? (rest children)))
                       nil

                       opts?
                       (map ->tag (rest children))

                       :else
                       (map ->tag children)))]
      (cond
        (keyword? tag)
        (str
         "<"  (name tag) " " opts ">"
         "\n" children-
         "</" (name tag) ">")

        (string? tag)
        (->str tag)

        (number? tag)
        tag

        :else
        (apply ->tag tag)))))


(comment

  (->tag :div)
  (apply ->tag [:div
                ;; {:style {:color :red}}
                ;; [:div "some"]
                ;; [:style {:a {:some :a}}]
                ])
  (apply ->tag [:body
                 [:div#id-example]
                 [:div#id.class-example]
                 [:div.class-example.multiple-class]
                 [:div {:a 10} "some"]
                 ;; [:div {} [:div "some2"]]
                ])

  (apply ->tag (clojure.edn/read-string (slurp "/home/veschin/work/ednml/src/core/index.edn")))
  (->tag :div {} "some")
  (->opts {:a    10
           :b    true
           :some :some-key
           :href "src://some-src"})

                                        ;
  )
