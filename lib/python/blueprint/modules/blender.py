"""Blender integration module"""
import os
import subprocess
import json

import blueprint.conf as conf

from blueprint.layer import Layer, SetupTask

class BlenderSetup(SetupTask):
    """
    The BlenderSetup task will introspect the .blend file and
    try to detect and register outputs with blueprint.
    """
    def __init__(self, layer, **kwargs):
        SetupTask.__init__(self, layer, **kwargs)

    def _execute(self):

        layer = self.getLayer()

        cmd = [conf.get("Blender", "bin")]
        cmd.append("-b")
        cmd.append(layer.getInput("scene_file").path)
        cmd.append("--python")
        cmd.append(os.path.join(os.path.dirname(__file__),
            "setup", "blender_setup.py"))

        output_path = "%s/blender_outputs_%d.json" % (self.getTempDir(), os.getpid())
        os.environ["PLOW_BLENDER_SETUP_PATH"] = output_path
        self.system(cmd)

        outputs = json.load(open(output_path, "r"))
        for output in outputs:
            layer.addOutput(output["pass"], output["path"], output)

class Blender(Layer):
    """
    The Blender module renders frames from a blender scene.
    """
    def __init__(self, name, **kwargs):
        Layer.__init__(self, name, **kwargs)
        self.requireArg("scene_file", (str,))

        self.addInput("scene_file",
            os.path.abspath(self.getArg("scene_file")))

    def _setup(self):
        self.addSetupTask(BlenderSetup(self))

    def _execute(self, frames):

        cmd = [conf.get("Blender", "bin")]
        cmd.append("-b")
        cmd.append(self.getInput("scene_file").path)
        cmd.append("-noaudio")
        cmd.append("-noglsl")
        cmd.append("-nojoystick")
        cmd.extend(("-t", os.environ.get("PLOW_THREADS", "1")))

        for f in frames:
            cmd.extend(("-f", str(f)))

        self.system(cmd)
