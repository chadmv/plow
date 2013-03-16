import os

import rpc.ttypes as ttypes

from conn import PlowConnection

Conn = PlowConnection()

__all__ = [
    "get_projects",
    "get_plow_time",
    "get_jobs",
    "get_active_job",
    "get_job",
    "kill_job",
    "launch_job",
    "pause_job",
    "get_job_outputs",
    "get_layer",
    "get_layer_by_name",
    "get_layer_outputs",
    "get_task",
    "get_tasks",
    "get_nodes",
    "get_node",
    "get_clusters",
    "get_cluster",
    "create_cluster",
    "delete_cluster",
    "lock_cluster",
    "set_cluster_name",
    "set_cluster_tags",
    "set_default_cluster",
    "retry_tasks",
    "kill_tasks",
    "eat_tasks"
]

def get_plow_time():
    return Conn.service.getPlowTime()

# Projects
def get_projects():
    return Conn.service.getProjects()

# Clusters

def create_cluster(name, tags):
    return Conn.service.createCluster(name, tags)

def get_cluster(name):
    return Conn.service.getCluster(name)

def delete_cluster(cluster):
    return Conn.service.deleteCluster(cluster.id)

def lock_cluster(cluster, value):
    return Conn.service.lockCluster(cluster.id, value)

def get_clusters(tag=None):
    if tag:
        return Conn.service.getClustersByTag(tag)
    else:
        return Conn.service.getClusters()

def set_cluster_name(cluster, name):
    Conn.service.setClusterName(cluster.id, name)

def set_cluster_tags(cluster, tags):
    Conn.service.setClusterTags(cluster.id, tags)

def set_default_cluster(cluster):
    Conn.service.setDefaultCluster(cluster.id)

# Jobs

def get_jobs(**kwargs):
    filt = ttypes.JobFilterT()
    for k, v in kwargs.items():
        setattr(filt, k, v)
    return Conn.service.getJobs(filt)

def get_active_job(name):
    return Conn.service.getActiveJob(name)

def get_job(guid):
    return Conn.service.getJob(guid)

def kill_job(job):
    Conn.service.killJob(job.id, "Manually killed by UID: %d " % os.getuid())

def launch_job(jobspec):
    return Conn.service.launch(jobspec)

def pause_job(job, paused):
    Conn.service.pauseJob(job.id, paused)

def get_job_outputs(job):
    return Conn.service.getJobOutputs(job.id)

# Layers

def get_layer(guid):
    return Conn.service.getLayerById(guid)

def get_layer_by_name(job, name):
    return Conn.service.getLayer(job.id, name)

def get_layer_outputs(job):
    return Conn.service.getLayerOutputs(job.id)

def add_layer_output(layer, path, attrs=None):
    if attrs is None:
        attrs = dict()
    Conn.service.addOutput(str(layer.id), str(path), attrs)

# Tasks

def get_task(guid):
    return Conn.service.getTask(guid)

def retry_tasks(**kwargs):
    filt = ttypes.TaskFilterT()
    for k, v in kwargs.items():
        setattr(filt, k, v)
    return Conn.service.retryTasks(filt)

def kill_tasks(**kwargs):
    filt = ttypes.TaskFilterT()
    for k, v in kwargs.items():
        setattr(filt, k, v)
    return Conn.service.killTasks(filt)

def eat_tasks(**kwargs):
    filt = ttypes.TaskFilterT()
    for k, v in kwargs.items():
        setattr(filt, k, v)
    return Conn.service.eatTasks(filt)

def get_tasks(**kwargs):
    filt = ttypes.TaskFilterT()
    for k, v in kwargs.items():
        setattr(filt, k, v)
    return Conn.service.getTasks(filt)

# Nodes

def get_node(name):
    return Conn.service.getNode(name)

def get_nodes(**kwargs):
    filt = ttypes.NodeFilterT()
    for k, v in kwargs.items():
        setattr(filt, k, v)
    return Conn.service.getNodes(filt)
