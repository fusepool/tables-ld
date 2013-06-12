#!/bin/bash

namespace="http://fusepool.info/";
tdbAssembler="/usr/lib/fuseki/tdb.fusepool.ttl";
JVM_ARGS="-Xmx32000M";
db="/data/tdb/fusepool/";
javatdbloader="java $JVM_ARGS tdb.tdbloader --desc=$tdbAssembler";
funding="/data/funding/";
#funding="../data/";
