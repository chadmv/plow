import os
import ConfigParser

__all__ = ["Parser"]

Parser = ConfigParser.RawConfigParser()

def _init():
    """
    Parse an initalize the Config object
    """
    if os.environ.get("BLUEPRINT_CFG"):
        cfgs = Parser.read([os.environ["BLUEPRINT_CFG"]])
    else:
        cfgs = Parser.read([
            os.path.join(os.environ.get("PLOW_ROOT", "/usr/local"), "etc/plow/blueprint.cfg"),
            os.path.expanduser("~/.plow/blueprint.cfg")])
    assert(cfgs)

# run as a function to avoid polluting module with temp variables
_init()

_BOOLEANS = frozenset(["true", "1", "on", "yes"])
def asBool(value):
    """
    Convert a string value into a boolean.
    """
    return value.lower() in _BOOLEANS

def get(section, key, **kwargs):
    """
    Return the specified configuration option.  Automatically
    interpolates any environement variables specified in plow.ini.
    """
    interps = Parser.get("env", "interpolate").split(",")
    args = dict([(inter, os.environ.get(inter, "test")) for inter in interps])
    if kwargs:
        args.update(kwargs)
    return Parser.get(section, key) % args
