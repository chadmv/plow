"""
Plow outputs plugin.

Registers outputs with the plow server.
"""
import os
import logging
logger = logging.getLogger(__name__)

import blueprint

def afterExecute(layer):

    import plow

    # When a setup task is complete outputs should
    # be registerd with its parent layer.
    if not isinstance(layer, (blueprint.SetupTask,)):
        return

    parent = layer.getLayer()
    logger.info("Registering %d outputs" % len(parent.getOutputs()))
    for output in parent.getOutputs():
        logger.info("Registering output with plow: %s" % output.path)
        plow_layer = plow.getLayerByName(
            os.environ.get("PLOW_JOB_ID"), parent.getName())
        plow.addLayerOutput(plow_layer, output.path, {})



