@echo off

:: Python
set "OUT=..\python\plow\rndaemon"
if exist "%OUT%\rpc" del /S /Q "%OUT%\rpc"
mkdir "%OUT%\rpc" 

call thrift --gen py:new_style:utf8strings -out %OUT% rnd.thrift
call thrift --gen py:new_style:utf8strings -out "%OUT%/rpc" common.thrift

set "OUT=..\python\src\core\rpc"

if exist "%OUT%" del /S /Q "%OUT%"
mkdir "%OUT%" 

call thrift --gen cpp --out "%OUT%" common.thrift
call thrift --gen cpp --out "%OUT%" plow.thrift

del "%OUT%\RpcService_server.skeleton.cpp"

:: Java
del /S /Q "..\java\src\main\java"
mkdir "..\java\src\main\java"
call thrift -gen java:java5 --out ../java/src/main/java common.thrift
call thrift -gen java:java5 -out ../java/src/main/java plow.thrift
call thrift -gen java:java5 -out ../java/src/main/java rnd.thrift

:: Javascript
:: Don't need common
call thrift -gen js:jquery -out ..\js plow.thrift

:: C++
set "OUT=..\cpp\src\core\rpc"
set "EXP=..\cpp\src\export\plow"

if exist "%OUT%" del /S /Q "%OUT%"
mkdir "%OUT%" 

call thrift --gen cpp --out "%OUT%" common.thrift
call thrift --gen cpp --out "%OUT%" plow.thrift

move "%OUT%\common_*.h" "%EXP%"
move "%OUT%\plow_*.h" "%EXP%"

del "%OUT%\RpcService_server.skeleton.cpp"

echo. & echo Successfully built Thrift bindings!
