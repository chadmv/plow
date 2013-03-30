#!/usr/bin/env python

import os
import glob
from subprocess import Popen, PIPE

from distutils.core import setup
from distutils.extension import Extension 
from distutils.sysconfig import get_config_vars

from Cython.Distutils import build_ext 


ROOT = os.path.dirname(os.path.abspath(__file__))

# Fix for GCC issues on Linux
opt = get_config_vars("OPT")[0].split()
exclude = set(["-Wstrict-prototypes"])
os.environ['OPT'] = " ".join(flag for flag in opt if flag not in exclude)

# Plow 
PLOW_INCLUDES = [os.path.join(ROOT, p) for p in ('src/core', 'src/core/rpc')]
PLOW_CPP = PLOW_INCLUDES

# build source
PLOW_SOURCE = [os.path.join(ROOT, "src/plow.pyx")]
for p in PLOW_CPP:
    PLOW_SOURCE += glob.glob(os.path.join(p, "*.cpp"))

# build includes
cflags = Popen(['pkg-config', '--cflags', 'thrift'], stdout=PIPE).communicate()[0].split()
cflags = [p.rstrip("/thrift") for p in cflags]

cflags.extend("-I%s" % p for p in PLOW_INCLUDES)

# build libs
ldflags = Popen(['pkg-config', '--libs', 'thrift'], stdout=PIPE).communicate()[0].split()

plowmodule = Extension('plow',
                        PLOW_SOURCE,
                        language="c++",
                        libraries=["boost_thread-mt"],
                        extra_compile_args=cflags,
                        extra_link_args=ldflags,
                        # https://issues.apache.org/jira/browse/THRIFT-1326
                        define_macros=[
                            ("HAVE_NETINET_IN_H", 1),
                            ("HAVE_INTTYPES_H", 1)
                        ]
                        )


cmdclass = {'build_ext': build_ext}

# Sphinx
try:
    from sphinx.setup_command import BuildDoc 
    cmdclass['build_sphinx'] = BuildDoc
except ImportError:
    pass


setup(name = 'plow',
      version = '0.1',
      author ='Justin Israel',
      author_email = 'justinisrael@gmail.com',
      description = 'Python client bindings for Plow server',
      ext_modules = [plowmodule],
      cmdclass = cmdclass
      )