(ns core.ednml
  (:require [clojure.string :as string]
            [css]
            [clojure.test :as t]))

(defn throw! [message]
  #?(:cljs (throw (js/Error. message))
     :clj (throw (Exception. message))))

(defmacro try! [body message]
  `(try
     ~body
     #?(:cljs (catch js/Error _# (do (println ~message) ~message))
        :clj (catch Exception _# (do (println ~message) ~message)))))

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

                  (vector? val)
                  (->str (string/join " " val))

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

(defn delete-chars [s re] (string/replace s re ""))

(defn preprocess-tag [tag]
  (if (<= (count (re-seq #"\#" (str tag))) 1)
    (try!
     (let [tag-str (str tag ".")
           tag     (re-find #":\w+" tag-str)
           id      (or (re-find #"\#.*?[\.|\#]" tag-str) "")
           classes (or (-> (cond-> tag-str
                             (not-empty id)
                             (string/replace id "."))
                           (delete-chars tag)
                           (string/split  #"\.")
                           rest
                           vec)
                       nil)]
       [(delete-chars tag #"^.")
        (delete-chars id #"^.|.$")
        classes])
     {:error (str "Error while parse tag -> " tag)})
     (throw! "Tag can have only one #id")))

(defn prepare-opts [tag [opts & _]]
  (let [[tag-
         id-
         classes-] (when (keyword? tag)
                     (preprocess-tag tag))
        id?        (not-empty id-)]
    (when (and id? (:id opts)) (throw! "Tag can have only one #id"))
    (let [opts (merge-with
                vector
                opts
                (cond-> {}
                  id?
                  (assoc :id id-)

                  (not-empty classes-)
                  (assoc :class classes-)))]
      [tag- (cond-> opts
              (:class opts)
              (update :class (comp vec flatten))

              :always
              ->opts)])))

(defn ->tag
  [tag & children]
  (try!
   (if (= :style tag)
     (str "<style>\n" (css/style-fn (first children)) "\n</style>\n")
     (let [children    (cond->> children
                         (-> children first map? not)
                         (cons {}))
           [tag- opts] (prepare-opts tag children)
           children-   (string/join
                        "\n"
                        (cond
                          (empty? (rest children))
                          nil

                          :else
                          (map ->tag (rest children))))]
       (cond
         (keyword? tag)
         (str
          "<"  (name tag-) " " opts ">"
          "\n" children- "\n"
          "</" (name tag-) ">\n")

         (string? tag)
         (->str tag)

         (number? tag)
         tag

         :else
         (apply ->tag tag))))
   (str "Error while parse tag -> " tag)))

(defn doctype [type] (str "<!DOCTYPE " (name type) ">\n"))

(defn ->html
  ([tags] (->html [:head [:title "Example page"] [:meta {:charset "UTF-8"}]] tags))
  ([head tags]
   (str (doctype :html)
        (apply ->tag [:html head tags]))))
