#!/usr/bin/env python


#-----------------------------------------------------------------------------
# Imports
#-----------------------------------------------------------------------------
import os
import sys
import glob
import shutil

import subprocess
from subprocess import Popen, PIPE
from ctypes.util import find_library

from distutils.core import setup, Command
from distutils.extension import Extension as dist_Extension
from distutils.sysconfig import get_config_vars

import doc.conf

#-----------------------------------------------------------------------------
# Setup variables and pre-checks
#-----------------------------------------------------------------------------

ROOT = os.path.dirname(__file__)

# Source files
PLOW_SOURCE_MAIN_PYX = os.path.join(ROOT, "src", "plow.pyx")
PLOW_SOURCE_MAIN_CPP = os.path.join(ROOT, "src", "plow.cpp")

PLOW_INCLUDES = [os.path.join(ROOT, p) for p in ('src/core', 'src/core/rpc')]
PLOW_CPP = PLOW_INCLUDES

PLOW_SOURCE_EXTRA = []
for p in PLOW_CPP:
    PLOW_SOURCE_EXTRA += glob.glob(os.path.join(p, "*.cpp"))

# Version
__version__ = doc.conf.release

# Check for cython
try:
    from Cython.Distutils import build_ext
except ImportError:
    use_cython = False
else:
    use_cython = True


if not os.path.exists(PLOW_SOURCE_MAIN_PYX):
    use_cython = False

if not use_cython and not os.path.exists(PLOW_SOURCE_MAIN_CPP):
    print "Error: Cython is not installed and the generated {0} source " \
          "file does not exist. \nNeed cython installed to continue.".format(PLOW_SOURCE_MAIN_CPP)
    sys.exit(1)

# Check for thrift generated sources
gen_dir = os.path.join(ROOT, "../thrift")
if os.path.exists(gen_dir):
    gen_cmd = "cd {0} && ./generate-sources.sh".format(gen_dir)
    print "Re-generating Thrift python client bindings"
    ret = subprocess.call(gen_cmd, shell=True)
    if ret != 0 and not os.path.exists(os.path.join(ROOT, "src/core/rpc")):
        print "Error: Missing the plow/client/core/rpc source file location.\n" \
              "Tried to generate, but Thrift command failed: {0}\n" \
              "Is Thrift installed?".format(gen_cmd)
        sys.exit(1)


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
    ldflags.append("-L{0}".format(THRIFT_LIB))
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


# set dylib ext:
if sys.platform.startswith('win'):
    lib_ext = '.dll'
elif sys.platform == 'darwin':
    lib_ext = '.dylib'
else:
    lib_ext = '.so'


# Fix for GCC issues on Linux
opt = get_config_vars("OPT")[0].split()
exclude = set(["-Wstrict-prototypes"])
os.environ['OPT'] = " ".join(flag for flag in opt if flag not in exclude)



#-----------------------------------------------------------------------------
# Extra commands
#-----------------------------------------------------------------------------

cmdclass = {}

if use_cython:

    class CythonCommand(build_ext):
        """Custom distutils command subclassed from Cython.Distutils.build_ext
        to compile pyx->c++, and stop there. All this does is override the 
        C++-compile method build_extension() with a no-op."""
        
        description = "Compile Cython sources to C++"
        
        def cython_sources(self, *args, **kwargs):
            # self.force = 1
            build_ext.cython_sources(self, *args, **kwargs)

        def build_extension(self, ext):
            pass

    cmdclass['build_ext'] = CythonCommand


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


cmdclass['clean'] = CleanCommand


# build docs
try:
    from sphinx.setup_command import BuildDoc 
    cmdclass['build_sphinx'] = BuildDoc
except ImportError:
    pass


# Utility functions
def get_data(*paths):
    data = []
    for p in paths:
        if not p.startswith(ROOT):
            p = os.path.join(ROOT, p)
        data.extend(glob.iglob(p))
    return data


def copy_dir(src, dst):
    if os.path.isdir(src):
        if os.path.isdir(dst):
            shutil.rmtree(dst)
        shutil.copytree(src, dst) 


#-----------------------------------------------------------------------------
# Extensions
#-----------------------------------------------------------------------------

#
# Cython client extension
#
script_args = set(sys.argv[1:])
for name in ('develop', 'sdist', 'build_sphinx', 'upload'):
    if name in script_args:
        use_cython = False

if use_cython:
    setup(
        name="plow",
        version=__version__,
        ext_modules=[
            dist_Extension('plow',
                           [PLOW_SOURCE_MAIN_PYX] + PLOW_SOURCE_EXTRA,
                           language="c++",
                           )
        ],
        cmdclass=cmdclass,
    )


#
# Python packages
#

# setuptools imports are delayed until after distutils, 
# because they monkey-patch stuff and break the cython
# Extension class
try:
    from python.ez_setup import use_setuptools
    use_setuptools()
except:
    pass

from setuptools import setup, find_packages, Extension

ROOT = os.path.dirname(__file__)
TEMP_BUILD_DIR = os.path.join(ROOT, '__dist__')

# manually graft in the parent etc/ directory so we can properly
# dist it from here
ETC_SRC_DIR = os.path.abspath(os.path.join(ROOT, '../../etc'))
ETC_DST_DIR = os.path.join(TEMP_BUILD_DIR, 'etc')
copy_dir(ETC_SRC_DIR, ETC_DST_DIR)

# BIN_SRC_DIR = os.path.abspath(os.path.join(ROOT, '../../bin'))
# BIN_DST_DIR = os.path.join(TEMP_BUILD_DIR, 'bin')
# copy_dir(BIN_SRC_DIR, BIN_DST_DIR)


plowmodule = Extension('plow.client.plow',
    [PLOW_SOURCE_MAIN_CPP] + PLOW_SOURCE_EXTRA,
    language="c++",
    libraries=[BOOST_PYTHON], 
    extra_compile_args=cflags,
    extra_link_args=ldflags,

    # https://issues.apache.org/jira/browse/THRIFT-1326
    define_macros=[
        ("HAVE_NETINET_IN_H", 1),
        ("HAVE_INTTYPES_H", 1)
    ]
)

setup(
    name="plow",
    version=__version__,

    packages=find_packages(exclude=['tests', 'tests.*']),

    dependency_links=[
        'https://github.com/sqlboy/blueprint/tarball/master#egg=blueprint-0.1',
        'https://github.com/sqlboy/fileseq/tarball/master#egg=fileseq-0.1',
        'http://peak.telecommunity.com/snapshots/',

    ],

    install_requires=[
        'psutil>=0.6.1',
        'argparse',
        'PyYAML',
        'blueprint==0.1',
        'fileseq==0.1',
    ],

    zip_safe = False,

    ext_modules = [plowmodule],

    # scripted functions that will get wrapped
    # into an entry point script
    entry_points={
        'console_scripts': [
            'rndaemon = plow.rndaemon.main:main',
        ],
        'gui_scripts': [
            'plow-wrangler = plow.gui.main:main'
        ]
    },

    # # stand-alone scripts from the root bin
    # scripts=[p for p in glob.glob(os.path.join(BIN_DST_DIR, "*")) if not p.endswith('rndaemon')],

    # # TODO: Some tests need to be made runable without an independant server
    test_suite="tests.test_all",

    # include_package_data=True,
    package_data={
        '__dist__': ["*"],
        'src': ["*.cpp", "*.h"],
        'plow': [
            "*.dat", "*.bp", "*.ini", "*.sh",
            'rndaemon/profile/*.dylib',
            'gui/resources/*.css',
            'gui/resources/icons.py'
        ],
    },

    data_files=[
        ("/usr/local/etc/plow", get_data(os.path.join(TEMP_BUILD_DIR, 'etc/plow/*.cfg'))),
    ],

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

# clean up
# shutil.rmtree(TEMP_BUILD_DIR, ignore_errors=True)


