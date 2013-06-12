#!/bin/bash
. ./tablesld.config.sh

/usr/lib/tarql/target/appassembler/bin/tarql topic.q > "$funding"import/funding.topics.ttl

./tdbstats.sh
