#!/bin/sh

mvn -e deploy:deploy-file \
    -DgroupId=postgresql \
    -DartifactId=postgresql \
    -Dversion=9.2-1002.jdbc4 \
    -Dpackaging=jar \
    -Dfile=postgresql-9.2-1002.jdbc4.jar \
    -Durl=file:///Users/${USER}/.m2/repository \
    -DrepositoryId=local
