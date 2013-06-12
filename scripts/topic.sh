#!/bin/bash
. ./tablesld.config.sh

/usr/lib/tarql/target/appassembler/bin/tarql topic.q > "$funding"funding.topics.ttl

./tdbstats.sh
