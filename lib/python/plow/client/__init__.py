from .plow import *

def _init():
    import conf
    from . import plow

    # Split the host name list and set it on the client module
    hosts = (h.split(":") for h in conf.PLOW_HOSTS)
    hosts = [(h[0], int(h[1])) for h in hosts if h[1].isdigit()]
    
    if not hosts:
        return

    PLOW_HOSTS[:] = hosts

_init()
del _init
