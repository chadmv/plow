
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
            list results

        try:
            getClient().proxy().getFolders(folders, self.project.id)
        except:
            results = []
            return results

        results = [initFolder(foldT) for foldT in folders]
        return results

cpdef get_project_by_id(Guid& guid):
    cdef: 
        ProjectT projT 
        Project project

    getClient().proxy().getProject(projT, guid)
    project = initProject(projT)
    return project


def get_project_by_name(string name):
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
        list results

    try:
        getClient().proxy().getProjects(projects)
    except:
        results = []
        return results

    results = [initProject(projT) for projT in projects] 
    return results

 