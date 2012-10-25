#!/usr/bin/env python

try:
    from ez_setup import use_setuptools
    use_setuptools()
except:
    pass

import os
from setuptools import setup, find_packages

__ROOT__ = os.path.dirname(__file__)

execfile(os.path.join(__ROOT__, 'plow/version.py'))

def get_description():
    README = os.path.abspath(os.path.join(__ROOT__, '../../README.md'))
    with open(README, 'r') as f:
        return f.read()


setup(

    name = "plow",
    version = __version__,
    packages = find_packages(),

    install_requires = [
        'psutil>=0.6.1',
        'thrift>=0.9.0'
    ],

    entry_points = {
        'console_scripts': [
            'rndaemon = plow.tools.rndaemon:main [OPTS]',
        ],
    },

    extras_require = {
        'OPTS':  ["argparse"],
    },

    # TODO: Some tests need to be made runable without an independant server
    test_suite = "test.tests_all",



    # Meta-stuff
    description='Python client for the Plow render farm',
    long_description=get_description(),
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