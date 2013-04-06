#!/bin/sh

# Python
########################################################################
OUT="../python/plow/rndaemon"
rm -rf ${OUT}/rpc
mkdir -p ${OUT}/rpc 

thrift --gen py:new_style:utf8strings -out $OUT rnd.thrift
thrift --gen py:new_style:utf8strings -out ${OUT}/rpc common.thrift

OUT="../python/src/core/rpc"

rm -rf $OUT
mkdir -p $OUT

thrift --gen cpp --out $OUT common.thrift
thrift --gen cpp --out $OUT plow.thrift

rm $OUT/RpcService_server.skeleton.cpp

cp plow__init__.py ../python/plow/client/__init__.py

# Java
########################################################################
rm -rf ../java/src/main/java/
mkdir -p ../java/src/main/java
thrift -gen java:java5 --out ../java/src/main/java common.thrift
thrift -gen java:java5 -out ../java/src/main/java plow.thrift
thrift -gen java:java5 -out ../java/src/main/java rnd.thrift

# Javascript
########################################################################
# Don't need common
thrift -gen js:jquery -out ../js plow.thrift

# C++
########################################################################

OUT="../cpp/src/core/rpc"
EXP="../cpp/src/export/plow"

rm -rf $OUT
mkdir $OUT

thrift --gen cpp --out $OUT common.thrift
thrift --gen cpp --out $OUT plow.thrift

mv $OUT/common_*.h $EXP
mv $OUT/plow_*.h $EXP

rm $OUT/RpcService_server.skeleton.cpp




