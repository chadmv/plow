#!/usr/bin/env python

try:
    from python.ez_setup import use_setuptools
    use_setuptools()
except:
    pass

import os
import glob
from setuptools import setup, find_packages

__ROOT__ = os.path.dirname(__file__)

execfile(os.path.join(__ROOT__, 'python/plow/version.py'))

def get_data(*paths):
    data = []
    for p in paths:
        data.extend(glob.glob(os.path.join(__ROOT__, p)))
    return data


setup(

    name = "PyPlow",
    version = __version__,

    package_dir = {'': 'python'},
    packages = find_packages('python'),

    install_requires = [
        'psutil>=0.6.1',
        'thrift>=0.9.0',
        'argparse',
        'PyYAML',
    ],

    entry_points = {
        'console_scripts': [
            'rndaemon = plow.tools.rndaemon:main',
        ],
    },

    # TODO: Some tests need to be made runable without an independant server
    test_suite = "plow.test.tests_all",

    include_package_data=True,
    package_data = {
        'plow': [
            'rndaemon/profile/*.dylib',
            'blueprint/*.ini',
        ],
    },

    # TODO: Force an installation of confs to /etc/plow ? (would imply sudo)
    data_files = [
        (os.path.expanduser('~/.plow/'), get_data('etc/*.cfg')),
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