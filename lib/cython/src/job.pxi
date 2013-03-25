
#######################
# JobState
#

@cython.internal
cdef class _JobState:
    cdef:
        readonly int INITIALIZE 
        readonly int RUNNING 
        readonly int FINISHED 

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

        # s.depends = self.depends

        return s

    property layers:
        def __get__(self): return self.layers
        def __set__(self, val): self.layers = val

    property depends:
        def __get__(self): return self.depends
        def __set__(self, val): self.depends = val

    def launch(self):
        cdef:
            JobT job
            JobSpecT spec
            Job ret 

        spec = self.toJobSpecT()
        getClient().proxy().launch(job, spec)

        ret = initJob(job)
        return ret


def launch(**kwargs):
    cdef:
        JobSpec spec 
        Job job 

    spec = JobSpec(**kwargs)
    job = spec.launch()
    return job


#######################
# Job
#
cdef inline Job initJob(JobT& j):
    cdef Job job = Job()
    job.setJob(j)
    return job


cdef class Job:

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
        def __get__(self):
            return self._job.maxRssMb

    def kill(self, string reason):
        return kill_job(self.id, reason)

    def pause(self, bint paused):
        pause_job(self.id, paused)

    def get_outputs(self):
        return get_job_outputs(self.id)

    def set_min_cores(self, int value):
        set_job_min_cores(self.id, value)

    def set_max_cores(self, int value):
        set_job_max_cores(self.id, value)


def get_job(Guid& id):
    cdef JobT jobT
    cdef Job job

    try:
        getClient().proxy().getJob(jobT, id)
    except RuntimeError:
        return None 

    job = initJob(jobT)
    return job

def get_active_job(string name):
    cdef JobT jobT
    cdef Job job

    try:
        getClient().proxy().getJob(jobT, name)
    except RuntimeError:
        return None 

    job = initJob(jobT)
    return job

def get_jobs(**kwargs):
    cdef: 
        vector[JobT] jobs 
        JobT jobT
        list ret 
        JobFilter filter = JobFilter(**kwargs)
        JobFilterT f = filter.value

    try:
        getClient().proxy().getJobs(jobs, f)
    except RuntimeError:
        ret = []
        return ret 

    ret = [initJob(jobT) for jobT in jobs]
    return ret

cpdef bint kill_job(Guid& id, string reason):
    cdef bint success
    success = getClient().proxy().killJob(id, reason)
    return success

cpdef inline pause_job(Guid& id, bint paused):
    getClient().proxy().pauseJob(id, paused)

cpdef inline set_job_min_cores(id, value):
    getClient().proxy().setJobMinCores(id, value)

cpdef inline set_job_max_cores(id, value):
    getClient().proxy().setJobMaxCores(id, value)

#######################
# Output
#

cdef Job initOutput(OutputT& o):
    cdef Output out = Output()
    out.setOutput(o)
    return out


cdef class Output:

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
    cdef:
        OutputT outT
        vector[OutputT] outputs
        list ret = []

    getClient().proxy().getJobOutputs(outputs, id)

    ret = [initOutput(outT) for outT in outputs]
    return ret


