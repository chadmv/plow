
import os
import ConfigParser

__all__ = ["Config"]

Config = ConfigParser.RawConfigParser()


def _init():
    """
    Parse an initalize the Config object
    """
    if os.environ.get("PLOW_CFG"):
        Config.read([os.environ["PLOW_CFG"]])
    else:
        Config.read(["/etc/plow/plow.cfg", os.path.expanduser("~/.plow/plow.cfg")])

    root = os.environ.get("PLOW_ROOT")
    if not root:
        root = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../'))

    if not 'env' in Config.sections():
        Config.add_section('env')
        
    Config.set('env', 'plow_root', root)

# run as a function to avoid polluting module with temp variables
_init()


assert Config.has_section("plow")

def get(section, key):
    """
    Return the specified configuration option.  Automatically
    interpolates any environement variables specified in plow.ini.
    """
    interps = Config.get("env", "interpolate").split(",")
    args = dict([(inter, os.environ.get(inter, "test")) for inter in interps])

    result = Config.get(section, key)
    return result % args


 