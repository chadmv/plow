Plow
====

Plow is a simple render farm.

Requirements
============

Server
------

* Postgresql 9.2
* Java7

Client and Tools
----------------

* Python 2.6 or 2.7 (sorry, talk to Autodesk)
* Qt 4.7+
* PySide 1.x
* Thrift 0.8 (going to 0.9 soon)


Running the Server
==================

Currently this is somewhat of a manual build process. In in the future well have a script for building a release.

You'll need Apache Maven to compile the Plow server, and a webapp container like Apache Tomcat to run it.  This
will most likely chanage to a runnable jar with embedded Jetty server in the future.

Setting up Postgres
-------------------

Install Postgresql 9.2.

Create a databse for plow, doesn't matter what its called.

Execute the sql file:

    > psql -h <hostname> -U <username> server/ddl/plow.sql


Geneating the Thrift Bindings
-----------------------------

Plow uses Apache Thrift for client/server communication.

To generate the bindings code for all languages:

    > cd client/thrift
    > ./generate-sources.sh

For Java, you then need to compile these sources and install the plow-bindings JAR into your local maven repo.  Running
mvn intall does this for you.

    > cd client/java
    > mvn install

Once you do that, you can now compile the server.

Compiling the Server
--------------------

    > cd server
    > mvn install
    > cp target/plow.war $TOMCAT_HOME/webapps
    > cp config/plow.properties $TOMCAT_HOME/lib

Now open $TOMCAT_HOME/lib/plow.properties and update the file with your postgres server info.


