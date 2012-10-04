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

Setting up Postgres
-------------------

Install Postgresql 9.2.

Create a databse for plow, doesn't matter what its called.

Execute the sql file:

    > psql -h <hostname> -U <username> server/ddl/plow.sql


Compiling the Server
--------------------

You'll need Apache Maven to compile the Plow server, and a webapp container like Apache Tomcat to run it.  This
will most likely chanage to a runnable jar with embedded Jetty server in the future.

    > cd server
    > mvn install
    > cp target/plow.war $TOMCAT_HOME/webapps
    > cp config/plow.properties $TOMCAT_HOME/lib

Now open $TOMCAT_HOME/lib/plow.properties and update the file with your postgres server info.


