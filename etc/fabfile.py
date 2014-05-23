import os
from fabric.api import cd, env, run, sudo

env.hosts = [os.environ['PLOW_HOST']]
env.user = "admin"

distdir = os.path.join(os.environ['PLOW_ROOT'], 'server', 'dist')

def stopdb():
    run('ps aux | grep "postgres -D" | grep -v "grep" | awk \'{print $2}\' | xargs kill')


def startdb():
    run('postgres -D /usr/local/var/postgres&')


def restartdb():
    stopdb()
    startdb()


def stopplow():
    run('ps aux | grep "plow.cfg" | grep -v "grep" | awk \'{print $2}\' | xargs kill')


def stoprndaemon():
    run('ps aux | grep rndaemon | grep -v "grep" | awk \'{print $2}\' | xargs kill')
