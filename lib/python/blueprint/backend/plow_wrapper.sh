#!/bin/bash
#
# Wraps the execution of plow tasks on the render farm.
#

# Set a temporary directory for the process.
export PLOW_TASK_TEMPDIR="/tmp/${PLOW_JOB_NAME}/${PLOW_TASK_NAME}"
export TMPDIR="$PLOW_TASK_TMPDIR"

# Make sure it exists
mkdir -p $PLOW_TASK_TEMPDIR

eval $@

# Clean up the temp directory
rm -f ${PLOW_TASK_TEMPDIR}/*


