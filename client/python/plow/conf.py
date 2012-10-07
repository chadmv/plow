
import os
import ConfigParser

__all__ = ["Config"]

Config = ConfigParser.RawConfigParser()

if os.environ.get("PLOW_CFG"):
    Config.read([os.environ["PLOW_CFG"]])
else:
    Config.read(["/etc/plow/plow.cfg", os.path.expanduser("~/.plow/plow.cfg")])

def get(section, key):
    """
    Return the specified configuration option.  Automatically
    interpolates any environement variables specified in plow.ini.
    """
    interps = Config.get("env", "interpolate").split(",")
    args = dict([(inter, os.environ[inter]) for inter in interps])

    result = Config.get(section, key)
    return result % args


 