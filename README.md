# EDN Markup Language
Write EDN, Compile to HTML
[Hiccup](https://github.com/weavejester/hiccup) ideological heir, but provide:  
- [x] standalone jar application  
- [x] watch mode  
- [x] support for `:div#id.class` syntax  
- [x] support for [veschin/css](https://github.com/veschin/edn-to-css) library in `[:style edn-map-style]` and `[:div {:style edn-inline-style}]`  

## Example

``` clojure
core.ednml/->html

(= [:div#id.my-class {:class "my"}]  
   ["<div" "class=\"my my-class\" id=\"id\">" "<div>"] 
   "<div class=\"my my-class\" id=\"id\"></div>")

(= [:style {:#id {:color :red}}]
   ["<style>" "#id {color: red;}" "</style>"]
   "<style> #id {color: red;} </style>")

(= (core.ednml/->html [:div [:span "example"]])
    ["<!DOCTYPE html>"
     "<html >"
     "<head >"
     "<meta charset=\"UTF-8\">"
     "</meta>"
     "</head>"
     "<div >"
     "<span >"
     "example"
     "</span>"
     "</div>"
     "</html>"]
    "<!DOCTYPE html>\n<html >\n<head >\n<meta charset=\"UTF-8\">\n\n</meta>\n\n</head>\n\n<div >\n<span >\nexample\n</span>\n\n</div>\n\n</html>\n")
```

## Jar usage example
 
### Build
``` sh 
git clone git@github.com:veschin/ednml.git

cd ednml

make -B build

java -jar build/ednml.jar -h
```

### Using as utility
``` sh
alias ednml="java -jar path/to/ednml/build/ednml.jar"

ednml -c -i i.edn -o o.html
```

- [x] `-c` Compile `-i` input-path `-o` output-path  
- [x] `-w` Watch `-i` input-path `-o` output-path  
