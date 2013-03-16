#!/usr/bin/env python

import os
import glob
from subprocess import Popen, PIPE

from distutils.core import setup
from distutils.extension import Extension 
from Cython.Distutils import build_ext 


ROOT = os.path.dirname(os.path.abspath(__file__))


# Plow 
PLOW_INCLUDES = [os.path.join(ROOT, p) for p in ('src/core', 'src/core/rpc')]
PLOW_CPP = PLOW_INCLUDES

# build source
PLOW_SOURCE = ["src/plow.pyx"]
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
                        extra_link_args=ldflags
                        )


setup(name = 'plow',
      version = '0.1',
      author ='Justin Israel',
      author_email = 'justinisrael@gmail.com',
      description = 'Python client bindings for Plow server',
      ext_modules = [plowmodule],
      cmdclass = {'build_ext': build_ext}
      )