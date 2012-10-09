#!/bin/sh

# Python
########################################################################
rm -rf plow/rpc
rm -rf rndlib/rpc

thrift --gen py:new_style:utf8strings -out ../python/plow plow.thrift
thrift --gen py:new_style:utf8strings -out ../python/rndlib rnd.thrift

thrift --gen py:new_style:utf8strings -out ../python/plow/rpc common.thrift
thrift --gen py:new_style:utf8strings -out ../python/rndlib/rpc common.thrift

# Java
########################################################################
rm -rf ../java/src/main/java/
mkdir -p ../java/src/main/java
thrift -gen java:java5 --out ../java/src/main/java common.thrift
thrift -gen java:java5 -out ../java/src/main/java plow.thrift
thrift -gen java:java5 -out ../java/src/main/java rnd.thrift
