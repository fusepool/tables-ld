#!/bin/bash
. ./tablesld.config.sh

java "$JVM_ARGS" tdb.tdbloader --desc="$tdbAssembler" --graph="$namespace"graph/funding "$funding"/import/funding.nt

./tdbstats.sh
