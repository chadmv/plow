
#######################
# JobState
#

@cython.internal
cdef class _JobState:
    cdef readonly int INITIALIZE, RUNNING, FINISHED

    def __cinit__(self):
        self.INITIALIZE = JOBSTATE_INITIALIZE
        self.RUNNING = JOBSTATE_RUNNING
        self.FINISHED = JOBSTATE_FINISHED

JobState = _JobState()


#######################
# JobFilter
#

@cython.internal
cdef class JobFilter:
    cdef JobFilterT value

    def __init__(self, **kwargs):
        self.value.matchingOnly = kwargs.get('matchingOnly', False)
        self.value.regex = kwargs.get('regex', '')
        self.value.project = kwargs.get('project', [])
        self.value.user = kwargs.get('user', [])
        self.value.jobIds = kwargs.get('jobIds', [])
        self.value.name = kwargs.get('name', [])

        cdef JobState_type i
        for i in kwargs.get('states', []):
            self.value.states.push_back(i)  


#######################
# JobSpec
#
cdef class JobSpec:
    """
    A JobSpec specifies the parameters for 
    launching a job

    :var name: str
    :var project: :class:`.Project`.id
    :var username: str - owner
    :var logPath: str - path to logfile
    :var paused: bool ; if True, submit in a paused state 
    :var uid: int - uid of owner 
    :var layers: list[:class:`.LayerSpec`]
    :var depends: list[:class:`.DependSpec`]

    """
    cdef public string name, project, username, logPath
    cdef public bint paused
    cdef public int uid
    cdef list layers, depends

    def __init__(self, **kwargs):
        self.name = kwargs.get('name', '')
        self.project = kwargs.get('project', '')
        self.username = kwargs.get('username', '')
        self.logPath = kwargs.get('logPath', '')
        self.paused = kwargs.get('paused', False)
        self.uid = kwargs.get('uid', 0)
        self.layers = kwargs.get('layers', []) 
        self.depends = kwargs.get('depends', [])

    def __repr__(self):
        return "<JobSpec: %s, %s>" % (self.name, self.project)

    cdef JobSpecT toJobSpecT(self):
        cdef JobSpecT s

        s.name = self.name 
        s.project = self.project 
        s.username = self.username 
        s.logPath = self.logPath
        s.paused = self.paused 
        s.uid = self.uid

        cdef:
            LayerSpec aLayer
            LayerSpecT specT

        for aLayer in self.layers:
            specT = aLayer.toLayerSpecT()
            s.layers.push_back(specT)

        cdef:
            DependSpec aDep
            DependSpecT depT

        for aDep in self.depends:
            depT = aDep.toDependSpecT()
            s.depends.push_back(depT)

        return s

    property layers:
        def __get__(self): return self.layers
        def __set__(self, val): self.layers = val

    property depends:
        def __get__(self): return self.depends
        def __set__(self, val): self.depends = val

    def launch(self):
        """
        Launch this spec and return the Job 

        :returns: :class:`.Job`
        """
        cdef:
            JobT job
            JobSpecT spec
            Job ret 

        spec = self.toJobSpecT()
        getClient().proxy().launch(job, spec)

        ret = initJob(job)
        return ret


#######################
# Job
#
cdef inline Job initJob(JobT& j):
    cdef Job job = Job()
    job.setJob(j)
    return job


cdef class Job:
    """
    A Job 

    :var id: str 
    :var folderId: str 
    :var name: str - name of the job 
    :var username: str - name of job owner
    :var uid: int - uid of job owner
    :var state: :obj:`.JobState`
    :var paused: bool
    :var minCores: int 
    :var maxCores: int 
    :var runCores: int 
    :var maxRssMb: int 
    :var startTime: long - msec since epoch
    :var stopTime: long - msec since epoch
    :var totals: :class:`.TaskTotals`

    """
    cdef:
        JobT _job 
        TaskTotals _totals

    def __init__(self):
        self._totals = None

    def __repr__(self):
        return "<Job: %s>" % self.name

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
        def __get__(self): return self._job.maxRssMb

    def kill(self, string reason):
        """
        Kill the job 

        :param reason: str - reason for killing
        """
        return kill_job(self.id, reason)

    def pause(self, bint paused):
        """
        Set the pause state of the job

        :param paused: bool
        """
        pause_job(self.id, paused)

    def get_outputs(self):
        """
        Get a list of outputs 

        :returns: list[:class:`.plowOutput`]
        """
        return get_job_outputs(self.id)

    def set_min_cores(self, int value):
        """
        Set the minimum cores the job should use 

        :param value: int 
        """
        set_job_min_cores(self.id, value)

    def set_max_cores(self, int value):
        """
        Set the maximum cores the job should use 

        :param value: int 
        """
        set_job_max_cores(self.id, value)


def launch_job(**kwargs):
    """
    Launch a job with a set of specs.
    Keyword parameters mirror those defined  
    as attributes of :class:`.JobSpec`

    :param name: str
    :param project: str :class:`.Project`.id
    :param username: str owner
    :param logPath: str path to logfile
    :param paused: bool ; if True, submit in a paused state 
    :param uid: int uid of owner 
    :param layers: list[:class:`.LayerSpec`]
    :param depends: list[:class:`.DependSpec`]
    :returns: :class:`.Job`
    """
    cdef:
        JobSpec spec 
        Job job 

    spec = JobSpec(**kwargs)
    job = spec.launch()
    return job
    
def get_job(Guid& id):
    """
    Get a :class:`.Job`

    :param id: str Job id
    :returns: :class:`.Job`
    """
    cdef JobT jobT
    cdef Job job

    try:
        getClient().proxy().getJob(jobT, id)
    except RuntimeError:
        return None 

    job = initJob(jobT)
    return job

def get_active_job(string name):
    """
    Get an active :class:`.Job`

    :param id: str name
    :returns: :class:`.Job`
    """    
    cdef JobT jobT
    cdef Job job

    try:
        getClient().proxy().getJob(jobT, name)
    except RuntimeError:
        return None 

    job = initJob(jobT)
    return job

def get_jobs(**kwargs):
    """
    Get a list of jobs matching a criteria.

    :param matchingOnly: bool
    :param regex: str regex pattern
    :param project: list[str] of matching project
    :param user: list[str] of matching user names
    :param jobIds: list[str] of matching job ids
    :param name: list[str] of matching job names

    :returns: list[:class:`.Job`]
    """
    cdef: 
        vector[JobT] jobs 
        JobT jobT
        list ret 
        JobFilter filter = JobFilter(**kwargs)
        JobFilterT f = filter.value

    getClient().proxy().getJobs(jobs, f)
    ret = [initJob(jobT) for jobT in jobs]
    return ret

cpdef bint kill_job(Guid& id, string reason):
    """
    Kill a job

    :param id: str Job id 
    :param reason: str reason for killing the job 
    :returns: bool success
    """
    cdef bint success
    success = getClient().proxy().killJob(id, reason)
    return success

cpdef inline pause_job(Guid& id, bint paused):
    """
    Set the pause state of a job

    :param id: str Job id 
    :param paused: bool pause state
    """
    getClient().proxy().pauseJob(id, paused)

cpdef inline set_job_min_cores(Guid& id, int value):
    """
    Set the minimum number of cores a job should get 

    :param id: str job id
    :param value: int number of cores
    """
    getClient().proxy().setJobMinCores(id, value)

cpdef inline set_job_max_cores(Guid& id, int value):
    """
    Set the maximum number of cores a job should get 

    :param id: str job id
    :param value: int number of cores
    """
    getClient().proxy().setJobMaxCores(id, value)

#######################
# Output
#

cdef Job initOutput(OutputT& o):
    cdef Output out = Output()
    out.setOutput(o)
    return out


cdef class Output:
    """
    Represents an output of a :class:`.Job`

    :var path: str path 
    :var attrs: dict attributes 
    
    """
    cdef public string path 
    cdef dict attrs

    def __init__(self):
        self.path = ''
        self.attrs = {} 

    cdef setOutput(self, OutputT& o):
        self.path = o.path
        self.attrs = o.attrs

    property attrs:
        def __get__(self): return self.attrs


cpdef get_job_outputs(Guid& id):
    """
    Get the outputs of a :class:`.Job`

    :param id: job id 
    :returns: list[:class:`.Output`]
    """
    cdef:
        OutputT outT
        vector[OutputT] outputs
        list ret = []

    getClient().proxy().getJobOutputs(outputs, id)

    ret = [initOutput(outT) for outT in outputs]
    return ret


