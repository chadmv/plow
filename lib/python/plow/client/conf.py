
import sys
import os
import ConfigParser

_Config = ConfigParser.RawConfigParser()
_Args = { }

def _init():
    """
    Parse an initalize the Config object
    """
    mod = sys.modules[__name__]

    if os.environ.get("PLOW_CFG"):
        parsed = _Config.read([os.environ["PLOW_CFG"]])
    else:
        parsed = _Config.read([
            os.path.join(os.environ.get("PLOW_ROOT", "/usr/local"), "etc/plow/plow.cfg"),
            os.path.expanduser("~/.plow/plow.cfg")])

    # If we parsed a config file, make sure its valid
    if parsed:
        assert _Config.has_section("plow"), ("Configuration is missing a 'plow' section: %r" % parsed)

    host_list = [h.strip() for h in get('plow', 'hosts', '').strip(',').split(',') if h]

    setattr(mod, 'PLOW_HOSTS', host_list or ["localhost:11336"])


def get(section, key, default=None):
    """
    Return the specified configuration option.
    """
    try:
        return _Config.get(section, key)
    except (ConfigParser.NoSectionError, ConfigParser.NoOptionError):
        return default


# run as a function to avoid polluting module with temp variables
_init()

 