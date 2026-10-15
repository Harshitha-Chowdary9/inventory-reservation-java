#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p build
javac --release 11 -d build src/*.java test/*.java
java -cp build InventoryTest
java -cp build Simulation
