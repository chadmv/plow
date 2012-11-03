import os
import yaml
import getpass

import plow
print dir(plow)
import blueprint.conf as conf

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

    base_name = runner.getJob().getName()
    job_name = conf.get("templates", "job_name", {"JOB_NAME": base_name})
    log_dir = conf.get("templates", "log_dir", {"JOB_NAME": base_name})

    job = runner.getJob()

    spec = plow.JobSpecT()
    spec.project = os.environ.get("PLOW_PROJECT",
        conf.get("defaults", "project"))
    spec.username = getpass.getuser()
    spec.uid = os.getuid()
    spec.paused = runner.getArg("pasued")
    spec.name =  job_name
    spec.logPath = log_dir
    spec.layers = []

    for layer in job.getLayers():
        lspec = plow.LayerSpecT()
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
        lspec.range = layer.getArg("frame_range", runner.getArg("frame_range", "1001"))
        lspec.chunk = layer.getArg("chunk", 1)
        lspec.minCores = layer.getArg("min_threads", 1)
        lspec.maxCores = layer.getArg("max_threads", 0)
        lspec.minMemory = layer.getArg("min_ram", 256)

        spec.layers.append(lspec)

    return spec

