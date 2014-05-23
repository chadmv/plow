import os
from fabric.api import cd, env, sudo

env.hosts = [os.environ['PLOW_HOST']]
env.user = "admin"

distdir = '$PLOW_ROOT/server/dist'

def stopdb():
    run('ps aux | grep "postgres -D" | grep -v "grep" | awk \'{print $2}\' | xargs kill')


def startdb():
    run('postgres -D /usr/local/var/postgres&')


def restartdb():
    stopdb()
    startdb()


def startplow():
    with cd(distdir):
        run('nohup java -Dplow.cfg.path=%s/resources/plow.properties -jar %s/winstone.jar --webappsDir=%s/webapps --httpPort=8081 > ~/plow_server.log 2>&1&' % (distdir, distdir, distdir))


def stopplow():
    run('ps aux | grep "plow.cfg" | grep -v "grep" | awk \'{print $2}\' | xargs kill')


def restartplow():
    stopplow()
    startplow()


def startrndaemon():
    run('nohup rndaemon > ~/plow_rndaemon.log 2>&1&')


def stoprndaemon():
    run('ps aux | grep rndaemon | grep -v "grep" | awk \'{print $2}\' | xargs kill')


def restartrndaemon():
    stoprndaemon()
    startrndaemon()
