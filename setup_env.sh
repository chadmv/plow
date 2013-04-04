#!/bin/sh

CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "Setting PLOW_ROOT: "$CWD
export PLOW_ROOT=$CWD

# Set python library path
export PYTHONPATH="$PYTHONPATH:$PLOW_ROOT/lib/python"

# Append plow executable path
export PATH="$PATH:$PLOW_ROOT/bin"

# Set config variables
export PLOW_RNDAEMON_CFG="$PLOW_ROOT/etc/plow/rndaemon.cfg"
export PLOW_CFG="$PLOW_ROOT/etc/plow/plow.cfg"
export BLUEPRINT_CFG="$PLOW_ROOT/etc/plow/blueprint.cfg"

# Set variables for blueprint job submission
export PROJECT="default_proj"
export SHOT="default_shot"