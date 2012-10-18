#!/bin/sh

# Python
########################################################################
rm -rf plow/rpc
rm -rf rndlib/rpc

thrift --gen py:new_style:utf8strings -out ../python/plow plow.thrift
thrift --gen py:new_style:utf8strings -out ../python/plow/rndaemon rnd.thrift

thrift --gen py:new_style:utf8strings -out ../python/plow/rpc common.thrift
thrift --gen py:new_style:utf8strings -out ../python/plow/rndaemon/rpc common.thrift

# Java
########################################################################
rm -rf ../java/src/main/java/
mkdir -p ../java/src/main/java
thrift -gen java:java5 --out ../java/src/main/java common.thrift
thrift -gen java:java5 -out ../java/src/main/java plow.thrift
thrift -gen java:java5 -out ../java/src/main/java rnd.thrift

# C++
########################################################################
rm -rf ../cpp/core/rpc
mkdir ../cpp/core/rpc
thrift --gen cpp --out ../cpp/core/rpc common.thrift
thrift --gen cpp --out ../cpp/core/rpc plow.thrift
