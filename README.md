Plow
====

Plow is render farm management software specifically designed for VFX workloads.

There are currently no official releases of Plow, so the following instructions will get
you started with a source checkout of Plow.  Please report any issues either directly on github, or in the Plow Users Google forums:
http://groups.google.com/group/plow-render

Developent Environment Requirements
===================================

Server
------

* [Postgresql](http://www.postgresql.org) 9.2
* [Java7](http://www.java.com/en/download/index.jsp)


Client and Tools
----------------

* Python 2.6/2.7
* [Qt](http://qt-project.org/downloads) 4.8
* [PySide](http://qt-project.org/wiki/Get-PySide) >= 1.1.x
* [Cython](http://www.cython.org/) >= 0.19 
* [Thrift](http://thrift.apache.org/) 0.9

Plow has an API for job submission, but the Blueprint library makes this much easier.

* [Blueprint](https://github.com/sqlboy/blueprint)

Installing the requirements
---------------------------

### Centos

Follow instructions [here](https://wiki.postgresql.org/wiki/YUM_Installation) to install Postgresql

```
sudo yum groupinstall -y 'development tools'
sudo yum install java-1.7.0-openjdk-devel.x86_64 postgresql93-contrib

# Install Maven
wget http://download.nextag.com/apache/maven/maven-3/3.2.1/binaries/apache-maven-3.2.1-bin.tar.gz
tar xvf apache-maven-3.2.1-bin.tar.gz
sudo mv apache-maven-3.2.1 /usr/local/apache-maven
```

Add to /etc/profile.d/maven.sh

```
export M2_HOME=/usr/local/apache-maven
export PATH=${M2_HOME}/bin:${PATH}
```

Install Thrift using these [instructions](https://thrift.apache.org/docs/install/centos)

```
pip install pyside
pip install Cython
pip install psutil
```

### Ubuntu/Mint

```
sudo apt-get install libpq-dev libpq5 python2.7-dev default-jdk maven
sudo apt-get install qt4-dev-tools libqt4-dev libqt4-core libqt4-gui
sudo apt-get install libboost-dev libboost-test-dev libboost-program-options-dev libboost-thread-dev
sudo apt-get libevent-dev automake libtool flex bison pkg-config g++ libssl-dev
# Postgres 9.2 is not available on Ubuntu/Mint yet so need to do the following to get 9.2
sudo su
echo 'deb http://apt.postgresql.org/pub/repos/apt/ precise-pgdg main' >> /etc/apt/sources.list.d/pgdg.list
wget --quiet -O - http://apt.postgresql.org/pub/repos/apt/ACCC4CF8.asc | apt-key add -
sudo apt-get update
sudo apt-get install postgresql-9.2 postgresql-contrib-9.2 pgadmin3
# Download thrift
tar -xvzf thrift-0.9.1.tar.gz
cd thrift-0.9.1
./configure
make
sudo make install
pip install pyside
pip install Cython
pip install psutil
```

### OSX

1. Install [JDK7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)

```
# Add to .bashrc
export JAVA_HOME=`/usr/libexec/java_home -v 1.7`

brew install postgresql
cp /usr/local/opt/postgresql/*.plist ~/Library/LaunchAgents/
brew install qt
brew install thrift
brew install maven
pip install pyside
pip install Cython
pip install psutil
pip install thrift
```

### Windows

There is currently no Windows support.  This is in-progress documentation for future support.

1. Install [Chocolatey](https://chocolatey.org)
2. Install [Boost binaries](http://sourceforge.net/projects/boost/files/boost-binaries/)

```
cinst java.jdk
cinst postgresql
cinst winflexbison
cinst thrift
cinst maven
# With VS 2012 installed
SET VS90COMNTOOLS=%VS110COMNTOOLS%
pip install PySide
pip install Cython
pip install psutil
```


Installing the Server
=====================

The plow server acts as the central brain for your render farm.  It contains the plow
dispatcher and exposes a thrift API for interacting with jobs.

The plow server code compiles to a Web Application Archive file, or WAR file.  This file
must be hosted by within a Java application server. 

While developing Plow, you will also need to compile your own WAR file at times.

Steps for Compiling the Server
------------------------------

### Install the Thrift Bindings

First, you must build the Plow thrift bindings for Java and install those in your local Maven repo.

    $ cd lib/thrift
    $ ./generate-sources.sh
    $ cd ../java
    $ mvn install

### Build the Server

Now you can actually build the server.

    $ cd server
    $ mvn package

This will create a file named target/plow.war.  Replace the plow.war file in the executable server package you
downloaded and your now on the latest version.  You can also copy (or symlink) plow.war into dist/webapps and
start it using dist/start-plow.sh:

    $ cp target/plow.war dist/webapps/

OR

    $ ln -s target/plow.war dist/webapps/

If the thrift files change at all, you have to re-generate + install the thrift bindings and recompile
the server.

Setting up Postgres
-------------------

Install Postgresql 9.2.

Create a database called 'plow' for user 'plow'.
set password: plow
(This is configurable in the plow-server-bin/resources/plow.properties )

```
sudo su - postgres
psql
CREATE USER plow SUPERUSER PASSWORD 'plow';
CREATE DATABASE plow OWNER plow;
\q
```

Execute the sql file:

    $ psql -h <hostname> -U <username> -d <dbname> -f server/ddl/plow-schema.sql

Install the Python Library and Tools
====================================

The latest Python client can be install from the source checkout using the following:

(first make sure to generate the thift bindings)

```
> cd lib/python
> python setup.py install
```

This will attempt to also copy the `etc/plow/*.cfg` files to `/usr/local/etc/plow/`. You may also manually copy them to 
`~/.plow/` instead if you want.

For Development...

```
> cd lib/python
> python setup.py develop
```

This will build the source files in place, and put a linked egg in your PYTHONPATH.

You can also set the PYTHONPATH path environment variable for development.

Starting the Server
-------------------

    > cd <plow source directory>/server/dist/
    > ./start-plow.sh

    If Java7 is not in your path, plow will pick it up if the JAVA_HOME env var is set.  On Mac, this will
    be something like this:

    > export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.7.0_10.jdk/Contents/Home"
    > ./start-plow.sh


Running the Tools
=================

First thing you need to do if you are using a git checkout of plow is setup your environment.

    $ export PLOW_ROOT="/path/to/plow/checkout"
    $ export PYTHONPATH="/path/to/plow/checkout/lib/python"
    $ export PATH="$PATH:$PLOW_ROOT/bin"

OR if you are in bash, you can just source setup_env.sh located on the root level.
This should set all your environment variables, including all config env variables.

    $ source  <plow source directory>/setup_env.sh


Running the Render Node Daemon
------------------------------

Currently supported on Mac/Linux

If you have installed the client tools using the `setup.py`, then you should now have `rndaemon` command in your path:

    $ rndaemon

Otherwise, you can launch rndaemon from your git checkout (after setting your environment variables and ensuring you have all python dependencies installed).

    $ <plow source directory>/bin/rndaemon

The daemon will first look for an optional config file explicitely set with the `PLOW_RNDAEMON_CFG` environment variable:

    $ export PLOW_RNDAEMON_CFG="/path/to/etc/plow/rndaemon.cfg"

Otherwise, it will search for: `/usr/local/etc/rndaemon.cfg`, `$PLOW_ROOT/etc/plow/rndaemon.cfg`, and then `~/.plow/rndaemon.cfg`


Running plow-wrangler GUI
------------------------------

After installing the python client, and having PySide installed, you should be able to run:

    $ plow-wrangler
    
    
