Plow
====

Plow is a simple render farm.

Requirements
============

Server
------

* Postgresql 9.2

http://www.postgresql.org

* Java7

http://www.java.com/en/download/index.jsp

Client and Tools
----------------

* Python 2.6 or 2.7 (sorry, talk to Autodesk)
* Qt 4.7+
* Thrift 0.9

Installing the Server
=====================

The plow server acts as the central brain for your render farm.  It contains the plow
dispatcher and exposes a thrift API for interacting with jobs.

This assumes your downloading the latest binary release from:
https://github.com/sqlboy/plow/downloads

Setting up Postgres
-------------------

Install Postgresql 9.2.

Create a databse for plow, doesn't matter what its called.

Execute the sql file:

    $ psql -h <hostname> -U <username> -d <dbname> -f ddl/plow-schema.sql
    $ psql -h <hostname> -U <username> -d <dbname> -f ddl/plow-triggers.sql
    $ psql -h <hostname> -U <username> -d <dbname> -f ddl/plow-functions.sql
    $ psql -h <hostname> -U <username> -d <dbname> -f ddl/plow-data.sql


Starting the Server
-------------------

    > tar -zxvf plow-server-bin-0.1-alpha.tar.gz
    > cd plow-server-bin-0.1-alpha
    > ./plow.sh

    If Java7 is not in your path, plow will pick it up if the JAVA_HOME env var is set.  On Mac, this will
    be something like this:

    > export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.7.0_10.jdk/Contents/Home"
    > ./plow.sh


Generating the Thrift Bindings
------------------------------

Plow uses Apache Thrift for client/server communication.  You can download thrift from here.

http://thrift.apache.org

To generate the bindings code for all languages:

    > cd client/thrift
    > ./generate-sources.sh

You can skip the next step if your using the plow server binary release.

For Java, you then need to compile these sources and install the plow-bindings JAR into your local maven repo.  Running
mvn intall does this for you.

    > cd client/java
    > mvn install


Install the Python Library and Tools
====================================

The latest Python client can be install from the source checkout using the following:

(first make sure to generate the thift bindings)

```
> cd client/python
> python setup.py install
```

You will still want to manually copy the `etc/*.cfg` files to either `/etc/plow/` or `~/.plow/`

Or you can use pip with one of the packaged releases at https://github.com/sqlboy/plow/downloads

`$ pip install https://github.com/downloads/sqlboy/plow/plow-client-python-<VERSION>.tar.gz`

This will also install the `etc/*.cfg` files to your `~/.plow/`



Running the Render Node Daemon
------------------------------

This is current supported on the Mac/Linux

If you have installed the client tools using the `setup.py`, then you should now have `rndaemon` command in your path.

Otherwise:

    > cd tools/rndaemon
    > ./rndaemon.py


The Plow Config File
--------------------

Before you can talk to the server with the plow client, you must setup the plow
configuration file.  In the source checkout, this can be found in client/etc/plow.cfg.

You can point plow at that configuration using the PLOW_CFG environment variable.

    > export PLOW_CFG="client/plow.plow.cfg"

Plow will also look for a configuration at /etc/plow/plow.cfg and ~/.plow/plow.cfg


Launching the Test Job
----------------------

Plow includes the blueprint module for job launching and description.

You'll need to set a couple environment vars to launch the test job:

    > export PROJECT="test"
    > export SHOT="test.01"

The full path to the plow configuration file.  You might want to actually
check out the configuration file and edit as necessary.

    > export PLOW_CFG="/plow.git/client/etc/plow.ini"

    > cd tools/plowrun
    > plowrun script.bp 1-100 -debug

