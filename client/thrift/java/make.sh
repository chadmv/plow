#!/bin/sh
rm -rf src/main/java/
mkdir -p src/main/java
thrift -gen java -out src/main/java ../plow.thrift
mvn install
