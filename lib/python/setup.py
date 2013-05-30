#!/usr/bin/env python


#-----------------------------------------------------------------------------
# Imports
#-----------------------------------------------------------------------------
import os
import sys
import glob
import shutil
import re 

import subprocess
from subprocess import Popen, PIPE
from ctypes.util import find_library

sys.path.insert(0, 'lib/fake_pyrex')


try:
    from lib.ez_setup import use_setuptools
    use_setuptools("0.6c12dev")
except:
    pass

from setuptools import setup, find_packages, Extension, Command
from setuptools.command.install import install as _install

from distutils.sysconfig import get_config_vars
from distutils.command.build_ext import build_ext
from distutils import log

import doc.conf

#-----------------------------------------------------------------------------
# Setup variables and pre-checks
#-----------------------------------------------------------------------------
__version__ = doc.conf.release

TEMP_BUILD_DIR = '__dist__'
ETC_SRC_DIR = os.path.abspath('../../etc')
ETC_DST_DIR = os.path.join(TEMP_BUILD_DIR, 'etc')
ETC_INSTALL_DIR = "/usr/local/etc/plow"

# Source files
PLOW_SOURCE_MAIN_PYX = os.path.join("src", "plow.pyx")
PLOW_SOURCE_MAIN_CPP = os.path.join("src", "plow.cpp")

PLOW_INCLUDES = ['src/core', 'src/core/rpc']
PLOW_CPP = PLOW_INCLUDES

PLOW_SOURCE_EXTRA = []
for p in PLOW_CPP:
    PLOW_SOURCE_EXTRA += glob.glob(os.path.join(p, "*.cpp"))


ldflags = []
cflags = []

# boost
BOOST_PYTHON = 'boost_thread-mt'
BOOST_LIB = os.getenv("BOOST_LIBRARY_PATH", "")

if not BOOST_LIB:
    boost_lib = find_library(BOOST_PYTHON)
    if boost_lib:
        BOOST_LIB = os.path.dirname(boost_lib)

if BOOST_LIB:
    ldflags.append("-L{0}".format(BOOST_LIB))

# Check for thrift
THRIFT_LIB = os.getenv("THRIFT_LIBRARY_PATH", "")
if THRIFT_LIB:
    ldflags.append("-L{0} -lthrift".format(THRIFT_LIB))
else:
    try:
        p = Popen(['pkg-config', '--libs', 'thrift'], stdout=PIPE)
    except:
        ret = 1
    else:
        ret = p.wait()
    if ret != 0:
        thrift_lib = find_library("thrift")
        if thrift_lib:
            ldflags.extend(["-L{0}".format(os.path.dirname(thrift_lib)), "-lthrift"])
        else:
            print "Error: Failed to locate thrift lib and THRIFT_LIBRARY_PATH is not set."
            sys.exit(1)
    else:
        ldflags.extend(p.communicate()[0].split())

# build includes
THRIFT_INCLUDE = os.getenv("THRIFT_INCLUDES_PATH", "")
if THRIFT_INCLUDE:
    cflags.append("-I{0}".format(THRIFT_INCLUDE))
else:
    try:
        p = Popen(['pkg-config', '--cflags', 'thrift'], stdout=PIPE)
        ret = p.wait()
    except:
        ret = 1
    else:
        ret = p.wait()
    if ret != 0:
        print "Error: Failed to locate thrift include dir and THRIFT_INCLUDES_PATH is not set."
        sys.exit(1)
    else:
        cflags.extend(p.rstrip("/thrift") for p in p.communicate()[0].split())


# Plow includes
cflags.extend("-I%s" % p for p in PLOW_INCLUDES)


# Fix for GCC issues on Linux
opt = get_config_vars("OPT")[0].split()
exclude = set(["-Wstrict-prototypes"])
os.environ['OPT'] = " ".join(flag for flag in opt if flag not in exclude)



# Check for cython
try:
    from Cython import __version__ as cython_version

    from distutils.version import StrictVersion

    if StrictVersion(cython_version) < StrictVersion("0.19"):
        print "[Warning] Cython version < 0.19 detected. Will try and build if .cpp exists already"
        raise ImportError
    else:
        from Cython.Build import cythonize

except ImportError:
    use_cython = False
else:
    use_cython = True

if not os.path.exists(PLOW_SOURCE_MAIN_PYX):
    use_cython = False


if not use_cython and not os.path.exists(PLOW_SOURCE_MAIN_CPP):
    print "Error: Cython >=0.19 is not installed and the generated {0} source " \
          "file does not exist. \nNeed cython installed to continue.".format(PLOW_SOURCE_MAIN_CPP)
    sys.exit(1)




plowmodule = Extension('plow.client.plow',
    PLOW_SOURCE_EXTRA,
    language="c++",
    libraries=[BOOST_PYTHON], 
    extra_compile_args=cflags,
    extra_link_args=ldflags,

    # https://issues.apache.org/jira/browse/THRIFT-1326
    define_macros=[
        ("HAVE_NETINET_IN_H", 1),
        ("HAVE_INTTYPES_H", 1),
        ("HAVE_CONFIG_H", 1),
    ]
)

argv = set(sys.argv[1:])

if use_cython:
    plowmodule.sources.insert(0, PLOW_SOURCE_MAIN_PYX)
    force = '-f' in argv or '--force' in argv
    ext_modules = cythonize([plowmodule], quiet=True, force=force)
    ext_modules[0].sources = [re.sub(r'\.c$', '.cpp', p) for p in ext_modules[0].sources]

else:
    plowmodule.sources.insert(0, PLOW_SOURCE_MAIN_CPP)
    ext_modules = [plowmodule]


#-----------------------------------------------------------------------------
# Extra commands
#-----------------------------------------------------------------------------
class CleanCommand(Command):
    """Custom distutils command to clean the .so and .pyc files."""

    user_options = [ ]

    def initialize_options(self):
        self._clean_me = []
        self._clean_trees = []
        for root, dirs, files in list(os.walk('plow')):
            for f in files:
                if os.path.splitext(f)[-1] in ('.pyc', '.so', '.o', '.pyd'):
                    self._clean_me.append(os.path.join(root, f))
            for d in dirs:
                if d == '__pycache__':
                    self._clean_trees.append(os.path.join(root, d))
        
        for d in ('build', ):
            if os.path.exists(d):
                self._clean_trees.append(d)

    def finalize_options(self):
        pass

    def run(self):
        for clean_me in self._clean_me:
            try:
                os.unlink(clean_me)
            except Exception:
                pass
        for clean_tree in self._clean_trees:
            try:
                shutil.rmtree(clean_tree)
            except Exception:
                pass


class InstallCommand(_install):

    def run(self):
        _install.run(self)

        self.announce("Running post-install", log.INFO)

        src_paths = set()
        exist_paths = set()
        src = os.path.join(ETC_DST_DIR, "plow")
        dst = ETC_INSTALL_DIR

        if not os.path.exists(dst):
            os.makedirs(dst)

        for name in os.listdir(src):
            src_path = os.path.join(src, name)
            dst_path = os.path.join(dst, name)

            if not os.path.exists(dst_path):
                self.copy_file(src_path, dst_path)  
            else:
                self.announce("{0} already exists".format(dst_path), log.INFO)


class ThriftCommand(Command):
    user_options = [ ]

    def initialize_options(self):
        pass

    def finalize_options(self):
        pass

    def run(self):
        gen_dir = "../thrift"
        if os.path.exists(gen_dir):
            gen_cmd = "cd {0} && ./generate-sources.sh".format(gen_dir)

            self.announce("Re-generating Thrift python client bindings", log.INFO)
            
            ret = subprocess.call(gen_cmd, shell=True)
            if ret != 0 and not os.path.exists("src/core/rpc"):
                err= "Error: Missing the plow/client/core/rpc source file location.\n" \
                      "Tried to generate, but Thrift command failed: {0}\n" \
                      "Is Thrift installed?".format(gen_cmd)
                self.announce(err, log.ERROR)
                sys.exit(1)


cmdclass = {
    'build_ext': build_ext,
    'clean' : CleanCommand,
    'install': InstallCommand,
    'build_thrift': ThriftCommand,
}

# build docs
try:
    from sphinx.setup_command import BuildDoc 
    cmdclass['build_sphinx'] = BuildDoc
except ImportError:
    pass


# Utility functions
def copy_dir(src, dst):
    if os.path.isdir(src):
        if os.path.isdir(dst):
            shutil.rmtree(dst)
        shutil.copytree(src, dst) 

#
# Python packages
#

# manually graft in the parent etc/ directory so we can properly
# dist it from here
copy_dir(ETC_SRC_DIR, ETC_DST_DIR)


setup(
    name="plow",
    version=__version__,

    packages=find_packages(exclude=['tests', 'tests.*']),

    install_requires=[
        'psutil>=0.6.1',
        'argparse',
        'PyYAML',
        'plow-blueprint>=0.1.1',
        'fileseq>=0.1',
    ],

    zip_safe = False,

    ext_modules=ext_modules,
    cmdclass=cmdclass,

    # # stand-alone scripts from the root bin
    scripts=glob.glob("bin/*"),

    # # TODO: Some tests need to be made runable without an independant server
    test_suite="tests.test_all",

    # include_package_data=True,
    package_data={
        # '__dist__': ["*"],
        'src': ["*.cpp", "*.h"],
        'plow': [
            "*.dat", "*.bp", "*.ini", "*.sh",
            'rndaemon/profile/*.dylib',
            'gui/resources/*.css',
            'gui/resources/icons.py'
        ],
    },

    # Meta-stuff
    author='Matt Chambers & Justin Israel',
    author_email='justinisrael@gmail.com',
    url='https://github.com/sqlboy/plow/',
    description='Python client for the Plow render farm',
    # long_description=get_description(),
    keywords=['render', 'renderfarm', 'management', 'queue', 'plow', 'visualfx',
                'vfx', 'visual', 'fx', 'maya', 'blender', 'nuke', '3dsmax', 'houdini'],
    platforms='POSIX / MacOS',
    classifiers=[
        'Development Status :: 3 - Alpha',
        'Environment :: Console',
        'Operating System :: MacOS :: MacOS X',
        'Operating System :: POSIX',
        'Operating System :: POSIX :: Linux',
        'Operating System :: POSIX :: BSD :: FreeBSD',
        'Programming Language :: C++',
        'Programming Language :: Python',
        'Programming Language :: Python :: 2.6',
        'Programming Language :: Python :: 2.7',
        'Topic :: System :: Monitoring',
        'Topic :: System :: Networking',
        'Topic :: System :: Networking :: Monitoring',
        'Topic :: Multimedia :: Graphics :: 3D Rendering',
        'Topic :: Utilities',
        'Topic :: Software Development :: Libraries',
        'Topic :: Software Development :: Libraries :: Python Modules',
        'Intended Audience :: Developers',
        'Intended Audience :: System Administrators',
    ],

)

