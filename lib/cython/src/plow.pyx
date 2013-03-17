from common_types cimport *
from plow_types cimport *
from client cimport getClient

from libcpp.vector cimport vector
from libcpp.string cimport string

from datetime import datetime

#######################
# General
#
def get_plow_time():
    cdef int epoch
    epoch = getClient().proxy().getPlowTime()
    plowTime = datetime.fromtimestamp(epoch / 1000)
    return plowTime


#######################
# Project
#
cdef Project initProject(ProjectT& p):
    cdef Project project = Project()
    project.setProject(p)
    return project


cdef class Project:

    cdef ProjectT project

    cdef setProject(self, ProjectT& proj):
        self.project = proj

    property id:
        def __get__(self):
            return self.project.id

    property name:
        def __get__(self):
            return self.project.name

    property title:
        def __get__(self):
            return self.project.title

    def get_folders(self):
        cdef:
            vector[FolderT] folders
            FolderT foldT 
            list results = []

        try:
            getClient().proxy().getFolders(folders, self.id)
        except:
            return results

        results = [initFolder(foldT) for foldT in folders]
        return results

def get_project_by_id(Guid& guid):
    cdef: 
        ProjectT projT 
        Project project

    getClient().proxy().getProject(projT, guid)
    project = initProject(projT)
    return project


def get_project_by_name(str name):
    cdef: 
        ProjectT projT 
        Project project

    getClient().proxy().getProjectByName(projT, name)
    project = initProject(projT)
    return project


def get_projects():
    cdef:
        vector[ProjectT] projects 
        ProjectT projT
        list results = []

    try:
        getClient().proxy().getProjects(projects)
    except:
        return results

    results = [initProject(projT) for projT in projects] 
    return results

#######################
# Folders
#

cdef Folder initFolder(FolderT& f):
    cdef Folder folder = Folder()
    folder.setFolder(f)
    return folder


cdef class Folder:

    cdef:
        FolderT folder
        TaskTotals _totals
        list _jobs

    def __init__(self):
        self._jobs = []
        self._totals = None

    def __repr__(self):
        return "<Folder: %s>" % self.name

    cdef setFolder(self, FolderT& f):
        cdef TaskTotalsT totals = self.folder.totals
        self.folder = f
        self._jobs = []
        self._totals = initTaskTotals(totals)

    property id:
        def __get__(self):
            return self.folder.id

    property name:
        def __get__(self):
            return self.folder.name

    property minCores:
        def __get__(self):
            return self.folder.minCores

    property maxCores:
        def __get__(self):
            return self.folder.maxCores

    property runCores:
        def __get__(self):
            return self.folder.runCores

    property order:
        def __get__(self):
            return self.folder.order

    property totals:
        def __get__(self):
            cdef TaskTotalsT totals

            if not self._totals:
                totals = self.folder.totals
                result = initTaskTotals(totals)
                self._totals = result

            return self._totals

    property jobs:
        def __get__(self):
            cdef JobT jobT

            if not self._jobs:
                self._jobs = [initJob(jobT) for jobT in self.folder.jobs]

            return self._jobs


def get_folder(Guid& folderId):
    cdef:
        FolderT folderT
        Folder folder 

    getClient().proxy().getFolder(folderT, folderId)
    folder = initFolder(folderT)
    return folder

def get_folders(Guid& projectId):
    proj = get_project_by_id(projectId)
    return Project.get_folders(proj)

def create_folder(Guid& projectId, str name):
    cdef: 
        FolderT folderT 
        Folder folder 

    getClient().proxy().createFolder(folderT, projectId, name)
    folder = initFolder(folderT)
    return folder

def get_job_board(Guid& projectId):
    cdef: 
        FolderT folderT 
        vector[FolderT] folders
        list ret

    getClient().proxy().getJobBoard(folders, projectId)
    ret = [initFolder(folderT) for folderT in folders]
    return ret


#######################
# Jobs
#

cdef Job initJob(JobT& j):
    cdef Job job = Job()
    job.setJob(j)
    return job


cdef class Job:

    cdef:
        JobT _job 
        TaskTotals _totals

    def __repr__(self):
        return "<Job: %s>" % self.name

    def __init__(self):
        self._totals = None

    cdef setJob(self, JobT& j):
        cdef TaskTotalsT totals = self._job.totals
        self._job = j
        self._totals = initTaskTotals(totals)

    property id:
        def __get__(self):
            return self._job.id

    property folderId:
        def __get__(self):
            return self._job.folderId

    property name:
        def __get__(self):
            return self._job.name

    property username:
        def __get__(self):
            return self._job.username

    property uid:
        def __get__(self):
            return self._job.uid

    property state:
        def __get__(self):
            return self._job.state

    property paused:
        def __get__(self):
            return self._job.paused

    property minCores:
        def __get__(self):
            return self._job.minCores

    property maxCores:
        def __get__(self):
            return self._job.maxCores

    property runCores:
        def __get__(self):
            return self._job.runCores

    property startTime:
        def __get__(self):
            return self._job.startTime

    property stopTime:
        def __get__(self):
            return self._job.stopTime

    property totals:
        def __get__(self):
            cdef TaskTotalsT totals

            if not self._totals:
                totals = self._job.totals
                result = initTaskTotals(totals)
                self._totals = result

            return self._totals

    property maxRssMb:
        def __get__(self):
            return self._job.maxRssMb

    def kill(self, str reason):
        return kill_job(self.id, reason)

    def pause(self, bint paused):
        pause_job(self.id, paused)

    # def get_outputs(self):
    #     return get_job_outputs(self.id)


# def get_job(Guid& id):
#     pass

# def get_active_job(str name):
#     pass


cdef class JobSpec:
    def __init__(self):
        raise NotImplementedError

# def launch(JobSpec spec):
#     pass


cdef class JobFilter:
    def __init__(self):
        raise NotImplementedError

# def get_jobs(JobFilter filter):
#     pass


cdef class Output:
    def __init__(self):
        raise NotImplementedError

# def get_job_outputs(Guid& id):
#     cdef:
#         OutputT outT
#         vector[OutputT] outputs
#         list ret = []
#
#     getClient().proxy().getJobOutputs(outputs, id)
#
#     ret = [initOutput(outT) fot outT in outputs]
#     return ret


def kill_job(Guid& id, str reason):
    cdef bint success
    success = getClient().proxy().killJob(id, reason)
    return success

def pause_job(Guid& id, bint paused):
    getClient().proxy().pauseJob(id, paused)

#######################
# Layers
#

# def get_layer_by_id(Guid& layerId):
#     pass

# def get_layer(Guid& jobId, str name):
#     pass

# def getLayers(Guid& jobId):
#     pass

# def add_layer_output(Guid& layerId, str path, Attrs& attrs):
#     pass

# def get_layer_outputs(Guid& layerId):
#     pass


#######################
# Tasks
#

cdef class Task:
    def __init__(self):
        raise NotImplementedError

# def get_task(Guid& taskId):
#     pass

# def get_tasks(TaskFilter filter):
#     getClient().proxy().getTasks()

def get_task_log_path(Guid& taskId):
    cdef: 
        string path
        str ret
    getClient().proxy().getTaskLogPath(path, taskId)
    ret = path 
    return ret

# def retry_tasks(TaskFilter filter):
#     pass

# def eat_tasks(TaskFilter filter):
#     pass

# def kill_tasks(TaskFilter filter):
#     pass


cdef TaskTotals initTaskTotals(TaskTotalsT& t):
    cdef TaskTotals totals = TaskTotals()
    totals.setTaskTotals(t)
    return totals


cdef class TaskTotals:

    cdef TaskTotalsT _totals

    cdef setTaskTotals(self, TaskTotalsT& t):
        self._totals = t 

    property total:
        def __get__(self):
            return self._totals.totalTaskCount

    property succeeded:
        def __get__(self):
            return self._totals.succeededTaskCount

    property running:
        def __get__(self):
            return self._totals.runningTaskCount

    property dead:
        def __get__(self):
            return self._totals.deadTaskCount

    property eaten:
        def __get__(self):
            return self._totals.eatenTaskCount

    property waiting:
        def __get__(self):
            return self._totals.waitingTaskCount

    property depend:
        def __get__(self):
            return self._totals.dependTaskCount

                                                            
#######################
# Nodes
#

cdef class Node:
    def __init__(self):
        raise NotImplementedError

# def get_node(str name):
#     cdef: 
#         NodeT nodeT
#         Node node 

#     getClient().proxy().getNode(nodeT, name)
#     node = initNode(nodeT)
#     return node

# def get_nodes(NodeFilter filter):
#     cdef:
#         NodeFilterT filterT 
#         NodeT nodeT
#         vector[NodeT] nodes 
#         list ret = []

#     getClient().proxy().getNodes(nodes, filterT)
#     ret = [initNode(nodeT) for nodeT in nodes]
#     return ret


#######################
# Cluster
#

cdef class Cluster:
    def __init__(self):
        raise NotImplementedError

# def get_cluster(str name):
#     cdef: 
#         ClusterT clusterT 
#         Cluster cluster 
#     getClient().proxy().getCluster(clusterT, name)
#     cluster = initCluster(clusterT)
#     return cluster

# def get_clusters():
#     cdef: 
#         ClusterT clusterT 
#         vector[ClusterT] clusters 
#         list ret = [] 
#     getClient().proxy().getClusters(clusters)
#     ret = [initCluster(clusterT) for clusterT in clusters]
#     return ret    

# def get_clusters_by_tag(str tag):
#     cdef: 
#         ClusterT clusterT 
#         vector[ClusterT] clusters 
#         list ret = [] 
#     getClient().proxy().getClusters(clusters, tag)
#     ret = [initCluster(clusterT) for clusterT in clusters]
#     return ret    

# def create_cluster(str name, list tags):
#     pass

def delete_cluster(Guid& clusterId):
    cdef bint ret
    ret = getClient().proxy().deleteCluster(clusterId)
    return ret

def lock_cluster(Guid& clusterId, bint locked):
    cdef bint ret
    ret = getClient().proxy().lockCluster(clusterId, locked)
    return ret

def set_cluster_tags(Guid& clusterId, list tags):
    pass

def set_cluster_name(Guid& clusterId, str name):
    getClient().proxy().setClusterName(clusterId, name)

def set_default_cluster(Guid& clusterId):
    getClient().proxy().setDefaultCluster(clusterId)


