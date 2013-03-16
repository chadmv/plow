from libcpp.vector cimport vector
from libcpp.string cimport string
from libcpp.set cimport set

from plow_types cimport *

cdef extern from "rpc/RpcService.h" namespace "Plow":
    cdef cppclass RpcServiceClient:
        int getPlowTime()
        void getProject(ProjectT&, Guid&)
        void getProjectByName(ProjectT&, string&) 
        void getProjects(vector[ProjectT]&) 
        void launch(JobT&, JobSpecT&) 
        void getActiveJob(JobT&, string&) 
        void getJob(JobT&,  Guid&) 
        bint killJob(Guid&, string&) 
        void pauseJob(Guid&, bool) 
        void getJobs(vector[JobT]&, JobFilterT&) 
        void getJobOutputs(vector[OutputT]&,  Guid&) 
        void createFolder(FolderT&, string&, string&) 
        void getFolder(FolderT&, string&) 
        void getJobBoard(vector[FolderT]&,  Guid&) 
        void getFolders(vector[FolderT]&, Guid&) 
        void getLayerById(LayerT&,  Guid&) 
        void getLayer(LayerT&,  Guid&, string&) 
        void getLayers(vector[LayerT]&,  Guid&) 
        void addOutput( Guid&, string&,  Attrs&) 
        void getLayerOutputs(vector[OutputT]&, Guid&) 
        void getTask(TaskT&,  Guid&) 
        void getTasks(vector[TaskT]&, TaskFilterT&) 
        void getTaskLogPath(string&, Guid&) 
        void getNode(NodeT&, string&) 
        void getNodes(vector[NodeT]&, NodeFilterT&) 
        void getCluster(ClusterT&, string&) 
        void getClustersByTag(vector[ClusterT]&, string&) 
        void getClusters(vector[ClusterT]&) 
        void createCluster(ClusterT&, string&, set[string]&) 
        bint deleteCluster(Guid&) 