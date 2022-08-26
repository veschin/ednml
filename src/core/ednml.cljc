(ns core.ednml
  (:require [clojure.string :as string]
            [css]))

(defn ->opts [opts]
  (string/join
   " "
   (for [[key val] opts
         :let [key (name key)
               ->str #(str "\"" % "\"")]]
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
          (string/join "="))
     )))

(defn ->tag
  ([tag] (->tag tag {} ""))
  ([tag child] (->tag tag {} child))
  ([tag opts & children]
   (str
    "<"  (name tag) " " (->opts opts) ">"
    " "  (apply ->tag children)
    "</" (name tag) ">")))


(comment


  (apply ->tag [:div {:style {:color :red}} [:div "some"]])
  (->opts {:style {:color :red}})
  (->opts {:a    10
           :b    true
           :some :some-key
           :href "src://some-src"})

                                        ;
  )
