import os
import yaml
import getpass
import pprint

import plow
import blueprint
import blueprint.conf as conf

def launch(runner):
    """
    Main entry point to launch plugin.
    """
    runner.setup()
    spec = serialize(runner)

    if runner.getArg("pretend"):
        pprint.pprint(spec)
    else:
        job = plow.launchJob(spec)
        runner.getJob().putData("jobid", job.id)


def createLayerSpec(layer):
    lspec = plow.LayerSpecT()
    lspec.name = layer.getName()
    lspec.tags =  layer.getArg("tags", ["unassigned"])
    lspec.chunk = layer.getArg("chunk", 1)
    lspec.minCores = layer.getArg("threads", 1)
    lspec.maxCores = layer.getArg("max_threads", 0)
    lspec.minRamMb = layer.getArg("ram", 512)

    return lspec

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

    # Task layers get created to store blueprint tasks.
    task_layers = { }

    for layer in job.getLayers():

        if isinstance(layer, (blueprint.Task,)):

            # Have to create a plow layer to store blueprint tasks.
            # This would be to org
            if not task_layers.has_key(layer.getArg("group")):
                task_layer = createLayerSpec(layer)
                task_layer.name = layer.getArg("group")
                task_layer.tasks = []
            else:
                task_layer = task_layers[layer.getGroup()]
                # Merge in the tags for the other layer.  Probably not the best option.
                task_layer.tags.update(layer.getArg("tags", set()))
                # Use the highest values on any task.
                task_layer.minCores = max(task_layer.minCores, task.getArg("threads", 1))
                task_layer.maxCores = max(task_layer.maxCores, task.getArg("max_threads", 0))
                task_layer.minRamMb = max(task_layer.minRamMb, task.getArg("ram"))
        
            task_layer.command = [
                "%s/plow_wrapper.sh" % os.path.dirname(__file__),
                "%s/bin/taskrun" % os.environ.get("PLOW_ROOT", "/usr/local"),
                "-debug",
                os.path.join(job.getPath(), "blueprint.yaml"),
                "-task",
                "%{TASK}"
            ]

            task = plow.TaskSpecT()
            task.name = layer.getName()
            task.depends = setupTaskDepends(job, layer) 
            task_layer.tasks.append(task)
            spec.layers.append(task_layer)

        else:

            lspec = createLayerSpec(layer)
            lspec.depends = setupLayerDepends(job, layer)
            lspec.range = layer.getArg("frame_range",
                runner.getArg("frame_range", None))
            lspec.command = [
                "%s/plow_wrapper.sh" % os.path.dirname(__file__),
                "%s/bin/taskrun" % os.environ.get("PLOW_ROOT", "/usr/local"),
                "-debug",
                os.path.join(job.getPath(), "blueprint.yaml"),
                "-layer",
                layer.getName(),
                "-range",
                "%{RANGE}"
            ]
            spec.layers.append(lspec)


    return spec

def setupLayerDepends(job, layer):
    result = []
    for depend in layer.getDepends():
        dspec = plow.DependSpecT()
        
        depend_on = job.getLayer(str(depend.dependOn))
        if isinstance(depend_on, (blueprint.Task,)):
            # Layer on Task
            dspec.type = plow.DependType.LAYER_ON_TASK
            dspec.dependentLayer = str(depend.dependent)
            dspec.dependOnTask = str(depend_on)
        else:
            # Layer on Layer + Task by Task
            if depend.type == blueprint.DependType.All:
                dspec.type = plow.DependType.LAYER_ON_LAYER
            elif depend.type == blueprint.DependType.ByTask:
                dspec.type = plow.DependType.TASK_BY_TASK
            else:
                raise Exception("Invalid layer depend type: %s"  % depend.type)
            dspec.dependentLayer = str(depend.dependent)
            dspec.dependOnLayer = str(depend.dependOn)

        result.append(dspec)
    return result


def setupTaskDepends(job, task):
    """
    Setup task dependencies.  Tasks can depend on other
    tasks or layers.
    """
    result = []
    for depend in task.getDepends():
        dspec = plow.DependSpecT()
        depend_on = job.getLayer(str(depend.dependOn))
        if isinstance(depend_on, (blueprint.Task,)):
            # Task on Task
            dspec.type = plow.DependType.TASK_ON_TASK
            dspec.dependentTask = str(task)
            dspec.dependOnTask = str(depend_on)
        else:
            # Task on Layer
            dspec.type = plow.DependType.TASK_ON_LAYER    
            dspec.dependentTask = str(task)
            dspec.dependOnLayer = str(depend.dependOn)
        result.append(dspec)
    return result





