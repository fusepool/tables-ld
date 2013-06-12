#!/bin/bash
. ./tablesld.config.sh

/usr/lib/tarql/target/appassembler/bin/tarql topics.q > "$funding"funding.topics.ttl
/usr/lib/tarql/target/appassembler/bin/tarql calls.q > "$funding"funding.calls.ttl

./tdbstats.sh
