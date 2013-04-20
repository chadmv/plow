Getting Started
***************

.. contents::

Plow
====

Plow is render farm management software specifically designed for VFX workloads.

There are currently no official releases of Plow, so the following instructions will get
you started with a source checkout of Plow.  Please report any issues on
our Google code page.

Google Project home: http://code.google.com/p/plow/

Developent Environment Requirements
===================================

Server
------

* Postgresql 9.2 - http://www.postgresql.org

* Java7 - http://www.java.com/en/download/index.jsp

Client and Tools
----------------

* Python 2.7
* Qt 4.8
* PySide 1.1.x
* Thrift 0.9

Plow has an API for job submission, but the Blueprint library makes this much easier.

* Blueprint - https://github.com/sqlboy/blueprint


Installing the Server
=====================

The plow server acts as the central brain for your render farm.  It contains the plow
dispatcher and exposes a thrift API for interacting with jobs.

The plow server code compiles to a Web Application Archive file, or WAR file.  This file
must be hosted by within a Java application server. For convinience an executable 
binary distribution with a current WAR can be found here:

http://code.google.com/p/plow/downloads

If your developing Plow, you will also need to compile your own WAR file at times.

Steps for Compiling the Server
------------------------------

Install the PostgreSQL Java Driver
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

For some reason this version isn't in Maven yet.   This will install the Postgres JDBC driver into
your local Maven repository::

    $ cd server/lib
    $ ./pginstall.sh

Install the Thrift Bindings
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Next step, must build the Plow thrift bindings for Java and install those in your local Maven repo::

    $ cd client/thrift
    $ ./generate-sources.sh
    $ cd ../java
    $ mvn install

Build the Server
^^^^^^^^^^^^^^^^

Now you can actually build the server::

    $ cd server
    $ mvn package

This will create a file named target/plow.war.  Replace the plow.war file in the executable server package you
downloaded and your now on the latest version.  You can also copy plow.war into dist/webapps and run
start it using dist/start-plow.sh.

If the thrift files change at all, you have to re-generate + install the thrift bindings and recompile
the server.

Setting up Postgres
-------------------

Install Postgresql 9.2.

(This is configurable in the `<plow-server-bin>/resources/plow.properties`)

#. Create a database called 'plow' for user 'plow'.
#. set password: plow
#. Execute the sql file::

    $ psql -h <hostname> -U <username> -d <dbname> -f ddl/plow-schema.sql

Install the Python Library and Tools
====================================

The latest Python client can be install from the source checkout using the following:
(first make sure to generate the thift bindings)::

	> cd lib/python
	> python setup.py install

You will still want to manually copy the `etc/*.cfg` files to either `/usr/local/etc/plow/` or `~/.plow/`

You can set the PYTHONPATH path environment variable for development.

Starting the Server
-------------------

::

    > tar -zxvf plow-server-bin-0.0.5-alpha.tar.gz
    > cd plow-server-bin-0.0.5-alpha
    > ./start-plow.sh

If Java7 is not in your path, plow will pick it up if the JAVA_HOME env var is set.  On Mac, this will
be something like this::

    > export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.7.0_10.jdk/Contents/Home"
    > ./start-plow.sh


Running the Tools
=================

First thing you need to do if you are using a git checkout of plow is setup your environment.::

    $ export PLOW_ROOT="/path/to/plow/checkout"
    $ export PYTHONPATH="/path/to/plow/checkout/lib/python"
    $ export PATH="$PATH:$PLOW_ROOT/bin"

OR if you are in bash, you can just source setup_env.sh located on the root level.
This should set all your environment variables, including all config env variables::

	> source setup_env.sh


Running the Render Node Daemon
------------------------------

Currently supported on Mac/Linux

If you have installed the client tools using the `setup.py`, then you should now have `rndaemon` command in your path::

    $ rndaemon

Otherwise, you can launch rndaemon from your git checkout (after setting your environment variables and ensuring you have all python dependencies installed).::

    $ bin/rndaemon

The daemon will first look for an optional config file explicitely set with the `PLOW_RNDAEMON_CFG` environment variable::

    $ export PLOW_RNDAEMON_CFG="/path/to/etc/plow/rndaemon.cfg"

Otherwise, it will search for: `/usr/local/etc/rndaemon.cfg`, `$PLOW_ROOT/etc/plow/rndaemon.cfg`, and then `~/.plow/rndaemon.cfg`


