import os
import ConfigParser

__all__ = ["Config"]

_Config = ConfigParser.RawConfigParser()

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

def get(section, key):
    """
    Return the specified configuration option.  Automatically
    interpolates any environement variables specified in plow.ini.
    """
    interps = _Config.get("env", "interpolate").split(",")
    args = dict([(inter, os.environ.get(inter, "test")) for inter in interps])

    result = _Config.get(section, key)
    return result % args