{:user
 {:plugins [[cider/cider-nrepl "0.16.0"]
            [jonase/eastwood "0.2.6" :exclusions [org.clojure/clojure]]
            [lein-cloverage "1.0.10" :exclusions [org.clojure/clojure]]
            [lein-cljfmt "0.5.7" :exclusions [org.clojure/clojure
                                              rewrite-cljs]]
            [lein-kibit "0.1.6" :exclusions [org.clojure/clojure]]]
  :dependencies [[cljfmt "0.5.7" :exclusions [org.clojure/clojure
                                              rewrite-cljs]]
                 [jonase/kibit "0.1.6" :exclusions [org.clojure/clojure]]]}}
