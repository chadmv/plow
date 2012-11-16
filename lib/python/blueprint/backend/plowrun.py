import os
import yaml
import getpass
import pprint

import plow
import blueprint.conf as conf

from blueprint import Layer

def launch(runner):
    """
    Main entry point to launch plugin.
    """
    runner.setup()
    spec = serialize(runner)

    if runner.getArg("pretend"):
        pprint.pprint(spec)
    else:
        job = plow.submitJobSpec(spec)
        runner.getJob().putData("jobid", job.id)

def serialize(runner):
    """
    Convert the job from the internal blueprint stucture to a plow JobSpec.
    """
    job = runner.getJob()
    base_name = runner.getArg("job_name", job.getName())
    job_name = conf.get("templates", "job_name", JOB_NAME=base_name)
    log_dir = conf.get("templates", "log_dir", JOB_NAME=base_name)
    
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
        lspec.tags =  layer.getArg("tags", ["unassigned"])
        lspec.range = layer.getArg("frame_range", runner.getArg("frame_range", "1001"))
        lspec.chunk = layer.getArg("chunk", 1)
        lspec.minCores = layer.getArg("min_threads", 1)
        lspec.maxCores = layer.getArg("max_threads", 0)
        lspec.minMemory = layer.getArg("min_ram", 256)

        lspec.command = [
            "%s/bin/taskrun" % os.environ.get("PLOW_ROOT", "/usr/local"),
            os.path.join(job.getPath(), "blueprint.yaml"),
            "-layer",
            layer.getName(),
            "-range",
            "%{FRAME}"
        ]
        
        lspec.depends = []

        for depend in layer.getDepends():

            dspec = plow.DependSpecT()
            if depend.type == layer.DependAll:
                dspec.type = plow.DependType.LAYER_ON_LAYER
            elif depend.type == layer.DependByTask:
                dspec.type = plow.DependType.TASK_BY_TASK
            else:
                raise Exception("Invalid layer depend type: %s"  % depend.type)

            if isinstance(depend.dependent, (Layer,)):
                dspec.dependentLayer = depend.dependent.getName()
            else:
                dspec.dependentLayer = str(depend.dependent)

            if isinstance(depend.dependOn, (Layer,)):
                dspec.dependOnLayer = depend.dependOn.getName()
            else:
                dspec.dependOnLayer = str(depend.dependOn)

            lspec.depends.append(dspec)

        spec.layers.append(lspec)

    return spec

