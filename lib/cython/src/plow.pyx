from common_types cimport *
from plow_types cimport *
from client cimport getClient

from libcpp.vector cimport vector


def getPlowTime():
    cdef int plowTime 
    plowTime = getClient().proxy().getPlowTime()
    return plowTime


#######################
# Project
#
cdef initProject(ProjectT& p):
    cdef Project project = Project(p.id, p.name, p.title)
    return project


cdef class Project:

    cdef public str id, name, title

    def __init__(self, str id="", str name="", str title=""):
        self.id = id
        self.name = name
        self.title = title


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

# def getProjects():
#     cdef:
#         vector[ProjectT] projects 
#         vector[Project] results

#     getClient().proxy().getProjects(projects)

    