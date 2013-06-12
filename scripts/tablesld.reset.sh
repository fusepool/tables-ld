#!/bin/bash
. ./tablesld.config.sh

rm -rf "$funding"import/*
./tablesld.sh
./tablesld.create.n-triples.sh
./tablesld.drop.graph.sh
./tablesld.tdbloader.sh
