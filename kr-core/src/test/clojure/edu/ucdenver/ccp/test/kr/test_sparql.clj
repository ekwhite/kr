(ns edu.ucdenver.ccp.test.kr.test-sparql
  (use clojure.test
       ;;clojure.test.junit

       edu.ucdenver.ccp.test.kr.test-kb

       edu.ucdenver.ccp.kr.variable
       edu.ucdenver.ccp.kr.kb
       edu.ucdenver.ccp.kr.rdf
       edu.ucdenver.ccp.kr.sparql)
  (import java.net.URI))

;;; --------------------------------------------------------
;;; constansts
;;; --------------------------------------------------------

(def uri-a (URI. "http://www.example.org/a"))
(def uri-p (URI. "http://www.example.org/p"))
(def uri-b (URI. "http://www.example.org/b"))
(def uri-x (URI. "http://www.example.org/x"))

(def test-triples-uri
  '((ex/a rdf/type         foaf/Person)
    (ex/a foaf/name        "Alice")
    (ex/a foaf/mbox        "<mailto:alice@example.com>")
    (ex/a foaf/mbox        "<mailto:alice@work.example>")
    (ex/a foaf/knows       ex/b)
    (ex/b rdf/type         foaf/Person)
    (ex/b foaf/name        "Bob")))


(def test-triples-6-1
  '((ex/a rdf/type         foaf/Person)
    (ex/a foaf/name        "Alice")
    (ex/a foaf/mbox        "<mailto:alice@example.com>")
    (ex/a foaf/mbox        "<mailto:alice@work.example>")
    (ex/b rdf/type         foaf/Person)
    (ex/b foaf/name        "Bob")))

(def test-triples-6-3
  '((ex/a foaf/name        "Alice")
    (ex/a foaf/homepage    "<http://work.example.org/alice/>")
    (ex/b foaf/name        "Bob")
    (ex/b foaf/mbox        "<mailto:bob@work.example>")))

(def test-triples-7
  '((ex/a dc10/title       "SPARQL Query Language Tutorial")
    (ex/a dc10/creator     "Alice")
    (ex/b dc11/title       "SPARQL Protocol Tutorial")
    (ex/b dc11/creator     "Bob")
    (ex/c dc10/title       "SPARQL")
    (ex/c dc11/title       "SPARQL (updated)")))

(def test-triples-10-2-1
  '((ex/a foaf/givenname   "Alice")
    (ex/a foaf/family_name "Hacker")
    (ex/b foaf/firstname   "Bob")
    (ex/b foaf/surname     "Hacker")))

(def test-triples-numbers-equality
  '((ex/a foaf/givenname   "Alice")
    (ex/a foaf/surname     "Hacker")
    (ex/a foaf/age         [40 xsd/integer])
    (ex/b foaf/firstname   "Bob")
    (ex/b foaf/surname     "Hacker")
    (ex/b foaf/age         40) ;the default should be xsd/integer
    (ex/c foaf/firstname   "Fred")
    (ex/c foaf/surname     "Hacker")
    (ex/c foaf/age         [50 xsd/integer])));50)))

(def test-triples-lang
  '((ex/a foaf/firstname   "Alice")
    (ex/b foaf/firstname   ["Bob" "en"])
    (ex/c foaf/firstname   ["Bob"])))

(def test-triples-custom-type
  '((ex/a ex/p             ["foo" ex/custom])
    (ex/b ex/p             ["foo" ex/custom2])))

(def test-triples-custom-type-uri
  `((ex/a ex/p             ["foo" ex/custom])
    (ex/b ex/p             ["foo" ~(URI. "http://www.example.org/custom")])))

;;; --------------------------------------------------------
;;; tests
;;; --------------------------------------------------------

(kb-test test-kb-loading-triples test-triples
         (is *kb*))

(kb-test test-simple-ask test-triples
         (is (ask '((_/person foaf/name ?/name)
                    (_/person foaf/mbox ?/email)))))

(kb-test test-simple-select test-triples
         (is (= 2
                (count (query '((_/person foaf/name ?/name)
                                (_/person foaf/mbox ?/email)))))))

(kb-test test-optional test-triples-6-1
         (is (= 3
                (count
                 (query '((?/person foaf/name ?/name)
                          (:optional
                           (?/person foaf/mbox ?/email))))))))

(kb-test test-count-query test-triples-6-1
         (is (= 3
                (count-query '((?/person foaf/name ?/name)
                               (:optional
                                (?/person foaf/mbox ?/email)))))))

(kb-test test-optional-select-6-3 test-triples-6-3
         (is (= 2
                (count 
                 (query '((?/person foaf/name ?/name)
                          (:optional (?/person foaf/mbox ?/email))
                          (:optional (?/person foaf/homepage ?/hpage))))))))
                       
(kb-test test-union-select-7 test-triples-7
         (is (= 2
                (count 
                 (query
                  '(:union
                    ((?/book dc10/title ?/title)
                     (?/book dc10/creator ?/author))
                    ((?/book dc11/title ?/title)
                     (?/book dc11/creator ?/author))))))))

(kb-test test-union-select-10-2-1 test-triples-10-2-1
         (is (= 2
                (count 
                 (query
                  '((:union ((?/x foaf/firstname   ?/gname))
                            ((?/x foaf/givenname   ?/gname)))
                    (:union ((?/x foaf/surname     ?/fname))
                            ((?/x foaf/family_name ?/fname)))))))))

(kb-test test-bound-operator test-triples-6-1
         (is (= 2 ;this is 2 but should be one if ?/person doesn't capture
                (count 
                 (query '((?/person foaf/name ?/name)
                          (:optional (?/person foaf/mbox ?/email))
                          (:filter (:bound ?/email)))))))
         (is (= 1 ;just bob
                (count 
                 (query '((?/person foaf/name ?/name)
                          (:optional (?/person foaf/mbox ?/email))
                          (:filter (:not (:bound ?/email)))))))))

(kb-test test-not-operator test-triples-6-1
         (is (= 1 ;just bob
                (count
                 (query '((?/person foaf/name ?/name)
                          (:optional (?/person foaf/mbox ?/email))
                          (:filter (:not (:bound ?/email))))))))
         (is (= 1 ;just bob
                (count
                 (query '((?/person foaf/name ?/name)
                          (:optional (?/person foaf/mbox ?/email))
                          (:filter (! (:bound ?/email)))))))))

(kb-test test-numbers test-triples-numbers-equality
         (is (= 2 ;two because of reflection
                (count (query '((?/person foaf/surname ?/name)
                                (?/person foaf/age ?/age1)
                                (?/person2 foaf/surname ?/name)
                                (?/person2 foaf/age ?/age2)
                                (:filter (= ?/age1 ?/age2))
                                (:filter (!= ?/person ?/person2)))))))
         (is (= 2
                (count (query '((?/person foaf/surname ?/name)
                                (?/person foaf/age ?/age1)
                                (?/person2 foaf/surname ?/name)
                                (?/person2 foaf/age ?/age2)
                                (:filter (> ?/age1 ?/age2))))))))

(kb-test test-n-ary-or test-triples-numbers-equality
         (is (= 3
                (count (query '((?/person foaf/surname ?/name)
                                (?/person foaf/age ?/age)
                                (:filter (:or (= ?/age 30)
                                              (= ?/age 40)
                                              (= ?/age 50)))))))))

(kb-test test-n-ary-and test-triples-numbers-equality
         (is (= 2 ;two because of reflection
                (count (query '((?/person foaf/surname ?/name)
                                (?/person foaf/age ?/age1)
                                (?/person2 foaf/surname ?/name)
                                (?/person2 foaf/age ?/age2)
                                (:filter (:and (= ?/age1 ?/age2)
                                               (!= ?/person ?/person2)))))))))

(kb-test test-boxed-number test-triples-numbers-equality
         (is (= 2
                (count (query '((?/person foaf/surname ?/name)
                                (?/person foaf/age 40))))))
         (is (= 0
                (count (query '((?/person foaf/surname ?/name)
                                (?/person foaf/age [40]))))))
         (is (= 2
                (count (query '((?/person foaf/surname ?/name)
                                (?/person foaf/age [40 xsd/integer]))))))

         (is (= 2
                (count (query '((?/person foaf/surname ?/name)
                                (?/person foaf/age ["40" xsd/integer])))))))

(kb-test test-lang test-triples-lang
         (testing "simple"
           (is (= 3
                  (count (query '((?/person foaf/firstname ?/x)))))))
         (testing "nested quotes"
           ;;TODO this langague tag is wrong there shouldn't need to be
           ;;      nested escaped quotes
           (is (= 2
                  (count (query '((?/person foaf/firstname ?/x)
                                  (:filter (= (:lang ?/x) ["en"]))))))))
         (testing "auto-magically set language as 'en'"
           (is (= 1
                  (count (query '((?/person foaf/firstname "Bob"))))))
           (is (= 1
                  (count (query '((?/person foaf/firstname "Alice")))))))
         (testing "force langugage as 'en'"
           (is (= 1
                  (count (query '((?/person foaf/firstname ["Alice" "en"])))))))
         (testing "boxed, unforced; language missing"
           ;; FIXME: Why do we allow this?  This leads to inconsistent
           ;; representation of literals.  Is "Alice" really not the same as
           ;; ["Alice"]?
           (is (= 0
                  (count (query '((?/person foaf/firstname ["Alice"])))))))
         (testing "exact match; 'bob' isn't 'Bob'"
           (is (= 0
                  (count (query '((?/person foaf/firstname "bob"))))))))

(kb-test test-query-visitor test-triples-lang
         (let [count (atom 0)]
           (query-visit (fn [bindings] (swap! count inc))
                        '((?/person foaf/firstname ?/x)))
           (is (= 3 @count)))

         (query-visit (fn [bindings] (is (= 2 (count bindings))))
                      '((?/person foaf/firstname ?/x)))

         (query-visit (fn [bindings]
                        (let [key-set (set (map first bindings))]
                          (is (key-set '?/x))
                          (is (key-set '?/person))))
                      '((?/person foaf/firstname ?/x))))

;; there is a bug where "3"^^<http://www.w3.org/2001/XMLSchema#integer>
;;  was coming out as "3" instead of 3
(kb-test test-integer-clj-ify test-triples-numbers-equality
         (is (= 40
                ('?/age (first (query '((?/person foaf/givenname "Alice")
                                        (?/person foaf/age ?/age))))))))

(kb-test test-backquote-operators test-triples-numbers-equality
         (is (= 2 ;two because of reflection
                (count (query `((?/person foaf/surname ?/name)
                                (?/person foaf/age ?/age1)
                                (?/person2 foaf/surname ?/name)
                                (?/person2 foaf/age ?/age2)
                                (:filter (= ?/age1 ?/age2))
                                (:filter (!= ?/person ?/person2)))))))
         (is (= 2
                (count (query `((?/person foaf/surname ?/name)
                                (?/person foaf/age ?/age1)
                                (?/person2 foaf/surname ?/name)
                                (?/person2 foaf/age ?/age2)
                                (:filter (> ?/age1 ?/age2))))))))

(kb-test test-strings-in-operators test-triples-6-3
         (is (= 2
                (count (query `((?/person foaf/name ?/name))))))
         (is (= 1
                (count (query `((?/person foaf/name ?/name)
                                (:filter (= "Bob" ?/name)))))))
         ;; box to drop the language tag since none given
         (is (= 0
                (count (query `((?/person foaf/name ?/name)
                                (:filter (= ["Bob"] ?/name))))))))

(kb-test test-regex-operator test-triples-6-3
         (is (= 2
                (count (query `((?/person foaf/name ?/name))))))
         (is (= 1
                (count (query `((?/person foaf/name ?/name)
                                (:filter (:regex ?/name "^ali" "i"))))))))

(kb-test test-uri-pat  test-triples-uri
         (is (= 1
                (count (query '((?/person1 foaf/knows ?/person2))))))
         (is (= 1
                (count (query '((ex/a foaf/knows ?/person2))))))
         (is (= 1
                (count (query '((?/person1 foaf/knows ex/b))))))
         (is (= 0
                (count (query '((ex/b foaf/knows ?/person2))))))
         (is (= 1
                (count (query `((?/person1 foaf/knows ~uri-b))))))
         (is (= 1
                (count (query `((~uri-a foaf/knows ?/person2))))))
         (is (ask `((~uri-a foaf/knows ~uri-b)))))

(kb-test test-custom-type  test-triples-custom-type
         (is (= 2
                (count (query '((?/person1 ex/p ?/custom))))))
         (is (= 1
                (count (query '((?/a ex/p ["foo" ex/custom])))))))

(kb-test test-custom-type-uri  test-triples-custom-type-uri
         (is (= 2
                (count (query '((?/person1 ex/p ?/custom))))))
         (is (= 2
                (count (query '((?/a ?/p ["foo" ex/custom]))))))
         (is (= 2
                (count
                 (query `((?/a
                           ?/p
                           ["foo" ~(URI. "http://www.example.org/custom")])))))))

(kb-test test-bind test-triples-uri
         (is (= '("Alice" "Bob")
                (sort (map #(get % '?/madeupname)
                           (query '((_/person foaf/name ?/name)
                                    (:bind (:as ?/name ?/madeupname)))))))))

(kb-test test-iri test-triples-uri
         (is (= 'pro://dom.ext
                (get (first (query '((:bind (:as (:iri ["pro://dom.ext"]) ?/name)))))
                     '?/name))))

(kb-test test-concat test-triples-uri
         (is (= "foobar"
                (get (first (query '((:bind (:as (:concat "foo" "bar") ?/x)))))
                     '?/x))))

(kb-test test-strbefore test-triples-uri
         (is (= "foo"
                (get (first (query '((:bind (:as (:strbefore "foobar" "bar") ?/x)))))
                     '?/x))))

(kb-test test-strafter test-triples-uri
         (is (= "bar"
                (get (first (query '((:bind (:as (:strafter "foobar" "foo") ?/x)))))
                     '?/x))))
