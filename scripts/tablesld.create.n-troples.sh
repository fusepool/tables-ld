#!/bin/bash
. ./tablesld.config.sh

for i in "$funding"*.ttl ; do rapper -g "$i" >> "$funding"import/funding.nt ; done
