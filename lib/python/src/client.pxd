from libcpp.vector cimport vector
from libcpp.string cimport string
from libcpp.set cimport set as c_set

from plow_types cimport *


cdef extern from "rpc/RpcService.h" namespace "Plow" nogil: 

    cdef cppclass RpcServiceClient:

        long getPlowTime() nogil except +

        void getServices(vector[ServiceT]&) nogil except +
        void createService(ServiceT&, ServiceT& svc) nogil except +
        void deleteService(Guid& id) nogil except +
        void updateService(ServiceT& svc) nogil except +

        void getProject(ProjectT&, Guid& id) nogil except +
        void getProjectByCode(ProjectT&, string& code) nogil except +
        void getProjects(vector[ProjectT]&) nogil except +
        void getActiveProjects(vector[ProjectT]&) nogil except +
        void createProject(ProjectT&, string& title, string& code) nogil except +
        void setProjectActive(Guid& id, bint active) nogil except +

        void launch(JobT&, JobSpecT& spec) nogil except +
        void getActiveJob(JobT&, string& name) nogil except +
        void getJob(JobT&,  Guid& jobId) nogil except +
        void killJob(Guid& jobId, string& reason) nogil except +
        void pauseJob(Guid& jobId, bint paused) nogil except +
        void getJobs(vector[JobT]&, JobFilterT& filter) nogil except +
        void getJobOutputs(vector[OutputT]&,  Guid& jobId) nogil except +
        void setJobMinCores(Guid& jobId, int value) nogil except +
        void setJobMaxCores(Guid& jobId, int value) nogil except +
        void getJobSpec(JobSpecT&, Guid& jobId) nogil except +

        void createFolder(FolderT&, string& projectId, string& name) nogil except +
        void getFolder(FolderT&, string& id) nogil except +
        void getJobBoard(vector[FolderT]&,  Guid& project) nogil except + 
        void getFolders(vector[FolderT]&, Guid& project) nogil except +
        void setFolderMinCores(Guid& folderId, int value) nogil except +
        void setFolderMaxCores(Guid& folderId, int value) nogil except +
        void setFolderName(Guid& folderId, string& name) nogil except +
        void deleteFolder(Guid& folderId) nogil except +

        void getLayerById(LayerT&,  Guid& layerId) nogil except +
        void getLayer(LayerT&,  Guid& jobID, string& name) nogil except +
        void getLayers(vector[LayerT]&,  Guid& layerId) nogil except +
        void getLayerOutputs(vector[OutputT]&, Guid& layerId) nogil except +
        void setLayerTags(Guid& guid, vector[string]& tags) nogil except +
        void setLayerMinCoresPerTask(Guid& guid, int minCores) nogil except +
        void setLayerMaxCoresPerTask(Guid& guid, int minCores) nogil except +
        void setLayerMinRamPerTask(Guid& guid, int minCores) nogil except +
        void setLayerThreadable(Guid& guid, bint threadable) nogil except +

        void addOutput(OutputT&, Guid& layerId, string& path, Attrs& attrs) nogil except +
        void updateOutputAttrs(Guid& outputId,  Attrs& attrs) nogil except +
        void setOutputAttrs(Guid& outputId, Attrs& attrs) nogil except +
        void getOutputAttrs(Attrs&, Guid& outputId) nogil except +


        void getTask(TaskT&,  Guid& taskId) nogil except +
        void getTasks(vector[TaskT]&, TaskFilterT& filter) nogil except +
        void getTaskLogPath(string&, Guid& taskId) nogil except +
        void retryTasks(TaskFilterT& filter) nogil except +
        void eatTasks(TaskFilterT& filter) nogil except +
        void killTasks(TaskFilterT& filter) nogil except +
        void getTaskStats(vector[TaskStatsT]&, Guid& taskId) nogil except +

        void getNode(NodeT&, string& name) nogil except +
        void getNodes(vector[NodeT]&, NodeFilterT& filter) nogil except +
        void setNodeLocked(Guid& id, bint locked) nogil except +
        void setNodeCluster(Guid& id, Guid& clusterId) nogil except +
        void setNodeTags(Guid& id, c_set[string]& tags) nogil except +
        void setNodeSlotMode(Guid& id, SlotMode_type mode, int slotCores, int slotRam) nogil except +

        void getProcs(vector[ProcT]&, ProcFilterT& filter) nogil except +
        void getProc(ProcT&, Guid& id) nogil except+

        void getCluster(ClusterT&, string& name) nogil except +
        void getClustersByTag(vector[ClusterT]&, string& tag) nogil except +
        void getClusters(vector[ClusterT]&) nogil except +
        void createCluster(ClusterT&, string& name) nogil except +
        bint deleteCluster(Guid& id) nogil except +
        bint lockCluster(Guid& id, bint locked) nogil except +
        void setClusterTags(Guid& id, c_set[string]& tags) nogil except +
        void setClusterName(Guid& id, string& name) nogil except +
        void setDefaultCluster(Guid& id) nogil except +

        void getQuota(QuotaT&,  Guid& id) nogil except +
        void getQuotas(vector[QuotaT]&, QuotaFilterT& filter) nogil except +
        void createQuota(QuotaT&,  Guid& projectId,  Guid& clusterId, int size, int burst) nogil except +
        void setQuotaSize( Guid& id, int size) nogil except +
        void setQuotaBurst( Guid& id, int burst) nogil except +
        void setQuotaLocked( Guid& id, bint locked) nogil except +

        void createFilter(FilterT&, Guid& projectId, string& name) nogil except +
        void getFilters(vector[FilterT]&, Guid& projectId) nogil except +
        void getFilter(FilterT&, Guid& filterId) nogil except +
        void deleteFilter(Guid& id) nogil except +
        void setFilterName(Guid& id, string& name) nogil except +
        void setFilterOrder(Guid& id, int order) nogil except +
        void increaseFilterOrder(Guid& id) nogil except +
        void decreaseFilterOrder(Guid& id) nogil except +

        void createFieldMatcher(MatcherT&, Guid& filterId, MatcherField_type field, MatcherType_type type, string& value) nogil except +
        void createAttrMatcher(MatcherT&, Guid& filterId, MatcherType_type type, string& attr, string& value) nogil except +

        void getMatcher(MatcherT&, Guid& matcherId) nogil except +
        void getMatchers(vector[MatcherT]&, Guid& filterId) nogil except +
        void deleteMatcher(Guid& id) nogil except +

        void createAction(ActionT&, Guid& filterId, ActionType_type type, string& value) nogil except +
        void deleteAction(Guid& id) nogil except +
        void getActions(vector[ActionT]&, Guid& filterId) nogil except +
        void getAction(ActionT&, Guid& actionId) nogil except +

        void getDependsOnJob(vector[DependT]&, Guid& jobId) nogil except +
        void getJobDependsOn(vector[DependT]&, Guid& jobId) nogil except +
        void getDependsOnLayer(vector[DependT]&, Guid& layerId) nogil except +
        void getLayerDependsOn(vector[DependT]&, Guid& layerId) nogil except +
        void getDependsOnTask(vector[DependT]&, Guid& taskId) nogil except +
        void getTaskDependsOn(vector[DependT]&, Guid& taskId) nogil except +
        void dropDepend(Guid& dependId) nogil except +
        void activateDepend(Guid& dependId) nogil except +
        void createDepend(DependT&, DependSpecT dependSpec) nogil except +
        void createJobOnJobDepend(Guid& jobId, Guid& onJobId) nogil except +
        void createLayerOnLayerDepend(Guid& layerId, Guid& onLayerId) nogil except +
        void createLayerOnTaskDepend(Guid& layerId, Guid& onTaskId) nogil except +
        void createTaskByTaskDepend(Guid& layerId, Guid& onLayerId) nogil except +
        void createTaskOnLayerDepend(Guid& taskId, Guid& onLayerId) nogil except +
        void createTaskOnTaskDepend(Guid& taskId, Guid& onTaskId) nogil except +


cdef extern from "client.h" namespace "Plow":
    cdef cppclass PlowClient:
        RpcServiceClient proxy() nogil 
        void reconnect() nogil except +

    cdef PlowClient* getClient() nogil except +
    cdef PlowClient* getClient(bint reset) nogil except +
    cdef PlowClient* getClient(string& host, int port) nogil except +
    cdef PlowClient* getClient(string& host, int port, bint reset) nogil except +

    cdef void resetClient() nogil except +
    