from plow import *

def _init():
    import conf

    hosts = conf.get('plow', 'hosts')
    if not hosts:
        return

    tokens = hosts.split(',')[0].split(':')

    if len(tokens) == 2 and tokens[1].isdigit():
        host, port = tokens 
        set_host(host, int(port))
    else:
        host = tokens[0]
        set_host(host)

_init()
del _init
