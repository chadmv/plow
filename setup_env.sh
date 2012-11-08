#!/bin/sh

CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "Setting PLOW_ROOT: "$CWD
export PLOW_ROOT=$CWD

# Set python library path
export PYTHONPATH="$PLOW_ROOT/lib/python"

# Append plow executable path
export PATH="$PATH:$PLOW_ROOT/bin"

# Set rndaemon config variable
export PLOW_RNDAEMON_CFG="$PLOW_ROOT/etc/plow/rndaemon.cfg"

# Set variables for blueprint job submission
export PROJECT="default_proj"
export SHOT="default_shot"