from common_types cimport *
from plow_types cimport *
from client cimport getClient

from libcpp.vector cimport vector



#######################
# General
#
def get_plow_time():
    cdef int plowTime 
    plowTime = getClient().proxy().getPlowTime()
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

        # for foldT in folders:
        #     results.append(initFolder(foldT))

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

    for projT in projects:
        results.append(initProject(projT))

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
                for jobT in self.folder.jobs:
                    self._jobs.append(initJob(jobT))

            return self._jobs


def get_folders(Project project):
    return Project.get_folders(project)

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


#######################
# Layers
#

#######################
# Tasks
#

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

