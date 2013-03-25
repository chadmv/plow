
#######################
# Project
#
cdef inline Project initProject(ProjectT& p):
    cdef Project project = Project()
    project.setProject(p)
    return project


cdef class Project:

    cdef ProjectT project

    cdef setProject(self, ProjectT& proj):
        self.project = proj

    property id:
        def __get__(self):
            cdef Guid id_ = self.project.id
            return id_

    property name:
        def __get__(self):
            cdef string code = self.project.code
            return code

    property title:
        def __get__(self):
            cdef string title = self.project.title
            return title

    property isActive:
        def __get__(self):
            cdef bint val = self.project.isActive
            return val

    def get_folders(self):
        cdef:
            vector[FolderT] folders
            FolderT foldT 
            list results

        try:
            getClient().proxy().getFolders(folders, self.project.id)
        except:
            results = []
            return results

        results = [initFolder(foldT) for foldT in folders]
        return results

    def set_active(self, bint active):
        set_project_active(self.id, active)

cpdef get_project_by_id(Guid& guid):
    cdef: 
        ProjectT projT 
        Project project

    getClient().proxy().getProject(projT, guid)
    project = initProject(projT)
    return project


def get_project_by_code(string code):
    cdef: 
        ProjectT projT 
        Project project

    getClient().proxy().getProjectByCode(projT, code)
    project = initProject(projT)
    return project


def get_projects():
    cdef:
        vector[ProjectT] projects 
        ProjectT projT
        list results

    try:
        getClient().proxy().getProjects(projects)
    except:
        results = []
        return results

    results = [initProject(projT) for projT in projects] 
    return results

def get_active_projects():
    cdef:
        vector[ProjectT] projects 
        ProjectT projT
        list results

    try:
        getClient().proxy().getActiveProjects(projects)
    except:
        results = []
        return results

    results = [initProject(projT) for projT in projects] 
    return results    

def create_project(string title, string code):
    cdef ProjectT projT
    cdef Project proj 
    getClient().proxy().createProject(projT, title, code)
    proj = initProject(projT)
    return proj

cpdef inline set_project_active(Guid& id, bint active):
    getClient().proxy().setProjectActive(id, active)


 