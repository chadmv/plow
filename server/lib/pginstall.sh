#!/bin/sh

mvn -e install:install-file \
    -DgroupId=postgresql \
    -DartifactId=postgresql \
    -Dversion=9.2-1002.jdbc4 \
    -Dpackaging=jar \
    -Dfile=postgresql-9.2-1002.jdbc4.jar
