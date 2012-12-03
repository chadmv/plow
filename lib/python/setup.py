#!/usr/bin/env python

try:
    from python.ez_setup import use_setuptools
    use_setuptools()
except:
    pass

import os
import glob
import shutil

from setuptools import setup, find_packages

ROOT = os.path.dirname(__file__)
TEMP_BUILD_DIR = os.path.join(ROOT, '__dist__') 

execfile(os.path.join(ROOT, 'plow/version.py'))


def get_data(*paths):
    data = []
    for p in paths:
        if not p.startswith(ROOT):
            p = os.path.join(ROOT, p)
        data.extend(glob.iglob(os.path.abspath(p)))
    return data


def copy_dir(src, dst):
    if os.path.isdir(src):
        if os.path.isdir(dst):
            shutil.rmtree(dst)
        shutil.copytree(src, dst)    

# manually graft in the parent etc/ directory so we can properly
# dist it from here
ETC_SRC_DIR = os.path.abspath(os.path.join(ROOT, '../../etc'))
ETC_DST_DIR = os.path.join(TEMP_BUILD_DIR, 'etc')
copy_dir(ETC_SRC_DIR, ETC_DST_DIR)

BIN_SRC_DIR = os.path.abspath(os.path.join(ROOT, '../../bin'))
BIN_DST_DIR = os.path.join(TEMP_BUILD_DIR, 'bin')
copy_dir(BIN_SRC_DIR, BIN_DST_DIR)

# open(os.path.join(BIN_DST_DIR, "__init__.py"), 'w').close()


setup(

    name="PyPlow",
    version=__version__,

    # package_dir={'': 'python'},
    packages=find_packages(exclude=['tests', 'tests.*']),

    install_requires=[
        'psutil>=0.6.1',
        'thrift>=0.9.0',
        'argparse',
        'PyYAML',
    ],

    # scripted functions that will get wrapped
    # into an entry point script
    entry_points={
        'console_scripts': [
            'rndaemon = plowapp.rndaemon.main:main',
        ],
    },

    # stand-alone scripts from the root bin
    scripts= \
        glob.glob(os.path.join(BIN_DST_DIR, "*")) + 
        [
            "blueprint/backend/plow_wrapper.sh",
        ],

    # TODO: Some tests need to be made runable without an independant server
    test_suite="tests.test_all",

    include_package_data=True,
    package_data={
        'plowapp': [
            'rndaemon/profile/*.dylib',
        ],
        'blueprint': [
            'backend/*.sh',
        ],
    },

    data_files=[
        ("/usr/local/etc/plow", get_data(os.path.join(TEMP_BUILD_DIR, 'etc/plow/*.cfg'))),
    ],

    # Meta-stuff
    description='Python client for the Plow render farm',
    # long_description=get_description(),
    keywords=['render', 'renderfarm', 'management', 'queue', 'plow', 'visualfx',
                'vfx', 'visual', 'fx', 'maya', 'blender', 'nuke', '3dsmax', 'houdini'],
    url='https://github.com/sqlboy/plow/',
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

#
# Post cleanup
#
shutil.rmtree(TEMP_BUILD_DIR)

