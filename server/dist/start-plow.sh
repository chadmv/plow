#!/bin/sh

PORT=8081
if [ -z "$JAVA_HOME" ]; then
    JAVA=`which java`
else
    JAVA=${JAVA_HOME}/bin/java
fi
echo "Using Java: $JAVA"
$JAVA -Dplow.cfg.path=${PWD}/resources/plow.properties -jar winstone.jar --webappsDir=webapps --httpPort=${PORT}
