(ns core.ednml-test
  (:require [core.ednml :refer [preprocess-tag
                                prepare-opts
                                ->tag]]
            [clojure.string :as string]
            [clojure.test :as t]))

(defn t-tag [tag expected]
  (t/is (=
         (preprocess-tag tag)
         expected)))

(t/deftest prepro-tag
  (t-tag :div
         ["div" "" []])

  (t-tag :div.class-example#id-example.multiple-class
         ["div" "id-example" ["class-example" "multiple-class"]])

  (t-tag :div#id-example
         ["div" "id-example" []])

  (t-tag :div.class-example
         ["div" "" ["class-example"]]))

(defn t-topts [tag children expected]
  (t/is (= (prepare-opts tag children)
           expected)))

(t/deftest prepro-opts
  (t-topts :div [{}]
           ["div" ""])

  (t-topts :div#id-example [{}]
           ["div" "id=\"id-example\""])

  (t-topts :div#id.my-class [{:class "my"}]
           ["div" "class=\"my my-class\" id=\"id\""]))

(defn debug-tag [tags]
  (->> tags
       (apply ->tag)
       (string/split-lines)
       (filter not-empty)))

(defn t->tag [tags expected]
  (t/is (= (debug-tag tags) expected)))

(t/deftest tag-test
  (t->tag [:div]
         ["<div >" "</div>"])

  (t->tag [:div.class]
         ["<div class=\"class\">" "</div>"])

  (t->tag [:style {:#id {:color :red}}]
         ["<style>" "#id {color: red;}" "</style>"])

  (t->tag [:div [:div "a" [:span "b"]] ]
         ["<div >" "<div >" "a" "<span >" "b" "</span>" "</div>" "</div>"]))

(comment

  (debug-tag [:i "1 2 3"])

;
  )
