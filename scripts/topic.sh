#!/bin/bash
. ./tablesld.config.sh

/usr/lib/tarql "$JVM_ARGS" topic.q > "$funding"import/funding.topics.ttl

./tdbstats.sh
