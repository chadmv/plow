import os
import yaml
import getpass

import plow
print plow.__file__

def launch(runner):
    """
    Main entry point to launch plugin.
    """
    runner.setup()
    spec = serialize(runner)

    plow.submitJobSpec(spec)

def serialize(runner):
    """
    Convert the job from the internal blueprint stucture to a plow JobSpec.
    """
    spec = plow.JobSpec()
    spec.project = runner.getArg("project")
    spec.username = getpass.getuser()
    spec.uid = os.getuid()
    spec.paused = runner.getArg("pasued")
    spec.name = runner.getArg("name", runner.getJob().getName())
    spec.logPath = config.get("defaults","log_dir")
    spec.layers = []

    for layer in job.getLayers():
        lspec = plow.LayerSpec()
        lspec.name = layer.getName()
        lspec.command =[
            os.path.join("taskrun"),
            os.path.join(job.getPath(), "blueprint.yaml"),
            "-layer",
            layer.getName(),
            "-range",
            "%{FRAME}"
        ]

        lspec.tags =  layer.getArg("tags", ["unassigned"])
        lspec.range = layer.getArg("frame_range", kwargs.get("frame_range", "1001"))
        lspec.chunk = layer.getArg("chunk", 1)
        lspec.minCores = layer.getArg("min_threads", 1)
        lspec.maxCores = layer.getArg("max_threads", 0)
        lspec.minMemory = layer.getArg("min_ram", 256)

        spec.layers.apppend(lspec)

    return spec

