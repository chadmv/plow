import os
import sys
import ConfigParser
import logging

__all__ = ['Config', 'get', 'getboolean', 'getint']


Config = ConfigParser.RawConfigParser()

logger = logging.getLogger(__name__)


def _init():
    """
    Parse an initalize the Config object
    Set module-level globals with the interpreted values.
    """
    if os.environ.has_key("PLOW_RNDAEMON_CFG"):
        cfgs = Config.read([os.environ["PLOW_RNDAEMON_CFG"]])
    else:
        cfgs = Config.read(["/etc/plow/rndaemon.cfg", os.path.expanduser("~/.plow/rndaemon.cfg")])

    if cfgs:
        logger.info('Config read from: %s' % cfgs)
    else:
        logger.warn('No config files found. Using default options')

    mod = sys.modules[__name__]

    #
    # rndaemon options
    #
    setattr(mod, 'NETWORK_DISABLED', getboolean('rndaemon', 'network_disabled', False))
    setattr(mod, 'NETWORK_PORT', getint('rndaemon', 'port', 11338))
    setattr(mod, 'NETWORK_PING_INTERVAL', getint('rndaemon', 'ping_interval', 60))

    hosts_str = get('rndaemon', 'plow_hosts', '')
    if hosts_str:
        setattr(mod, 'PLOW_HOSTS', [h.strip() for h in hosts_str.strip(',').split(',')])
    # TODO: This should not be needed outside of the dev environment
    # It would probably be a failure to not explicitely have a location of
    # the Plow host.
    else:
        setattr(mod, 'PLOW_HOSTS', ["localhost:11337"])

    #
    # task options
    #
    setattr(mod, 'TASK_PROXY_USER', get('task', 'proxy_user', ''))


def get(section, key, default=None):
    """
    Return the specified configuration option.
    """
    try:
        return Config.get(section, key)
    except (ConfigParser.NoSectionError, ConfigParser.NoOptionError):
        return default

def getboolean(section, key, default=None):
    """
    Return the specified configuration option as a boolean.
    """
    try:
        return Config.getboolean(section, key)
    except (ConfigParser.NoSectionError, ConfigParser.NoOptionError):
        return default


def getint(section, key, default=None):
    """
    Return the specified configuration option as a int.
    """
    try:
        return Config.getint(section, key)
    except (ConfigParser.NoSectionError, ConfigParser.NoOptionError):
        return default

# Init the config at import time
_init()
