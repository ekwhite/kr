#!/bin/bash

echo starting CIDER nREPL

pwd
mvn -e -f pom.xml \
    -Dclojure.vmargs="-d64 -Xmx2G" \
    -Dclojure.runwith.test=true \
    -Dclojure.mainClass="edu.ucdenver.ccp.kr.dev.cider_nrepl" \
    clojure:run
