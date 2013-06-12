#!/bin/bash
. ./tablesld.config.sh

java -Xmx16000M tdb.tdbupdate --desc="$tdbAssembler" 'DROP GRAPH <'"$namespace"'graph/funding>'
