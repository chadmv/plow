#!/bin/sh

# Python
########################################################################
# Client side tools
rm -rf plow/rpc
thrift --gen py:new_style:utf8strings -out ../python/plow plow.thrift

#Render Node Daemon
rm -rf rndlib/rpc
thrift --gen py:new_style:utf8strings -out ../python/rndlib rnd.thrift

# Java
########################################################################
rm -rf ../java/src/main/java/
mkdir -p ../java/src/main/java
thrift -gen java:java5 -out ../java/src/main/java plow.thrift
thrift -gen java:java5 -out ../java/src/main/java rnd.thrift
