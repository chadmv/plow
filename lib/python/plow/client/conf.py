
import os
import ConfigParser

_Config = ConfigParser.RawConfigParser()
_Args = { }

def _init():
    """
    Parse an initalize the Config object
    """
    if os.environ.get("PLOW_CFG"):
        _Config.read([os.environ["PLOW_CFG"]])
    else:
        _Config.read([
            os.path.join(os.environ.get("PLOW_ROOT", "/usr/local"), "etc/plow/plow.cfg"),
            os.path.expanduser("~/.plow/plow.cfg")])

# run as a function to avoid polluting module with temp variables
_init()

assert _Config.has_section("plow")

def get(section, key, default=None):
    """
    Return the specified configuration option.
    """
    try:
        return _Config.get(section, key)
    except (ConfigParser.NoSectionError, ConfigParser.NoOptionError):
        return default


 