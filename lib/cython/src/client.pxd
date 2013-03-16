from libcpp.vector cimport vector
from libcpp.string cimport string
from libcpp.set cimport set

from plow_types cimport *


cdef extern from "rpc/RpcService.h" namespace "Plow":
    cdef cppclass RpcServiceClient:
        int getPlowTime() nogil except +
        void getProject(ProjectT&, Guid&) nogil except +
        void getProjectByName(ProjectT&, string&) nogil except +
        void getProjects(vector[ProjectT]&) nogil except +
        void launch(JobT&, JobSpecT&) nogil except +
        void getActiveJob(JobT&, string&) nogil except +
        void getJob(JobT&,  Guid&) nogil except +
        bint killJob(Guid&, string&) nogil except +
        void pauseJob(Guid&, bool) nogil except +
        void getJobs(vector[JobT]&, JobFilterT&) nogil except +
        void getJobOutputs(vector[OutputT]&,  Guid&) nogil except +
        void createFolder(FolderT&, string&, string&) nogil except +
        void getFolder(FolderT&, string&) nogil except +
        void getJobBoard(vector[FolderT]&,  Guid&) nogil except + 
        void getFolders(vector[FolderT]&, Guid&) nogil except +
        void getLayerById(LayerT&,  Guid&) nogil except +
        void getLayer(LayerT&,  Guid&, string&) nogil except +
        void getLayers(vector[LayerT]&,  Guid&) nogil except +
        void addOutput( Guid&, string&,  Attrs&) nogil except +
        void getLayerOutputs(vector[OutputT]&, Guid&) nogil except +
        void getTask(TaskT&,  Guid&) nogil except +
        void getTasks(vector[TaskT]&, TaskFilterT&) nogil except +
        void getTaskLogPath(string&, Guid&) nogil except +
        void getNode(NodeT&, string&) nogil except +
        void getNodes(vector[NodeT]&, NodeFilterT&) nogil except +
        void getCluster(ClusterT&, string&) nogil except +
        void getClustersByTag(vector[ClusterT]&, string&) nogil except +
        void getClusters(vector[ClusterT]&) nogil except +
        void createCluster(ClusterT&, string&, set[string]&) nogil except +
        bint deleteCluster(Guid&) nogil except +


cdef extern from "client.h" namespace "Plow":
    cdef cppclass PlowClient:
        RpcServiceClient proxy()

    cdef PlowClient* getClient() nogil except +