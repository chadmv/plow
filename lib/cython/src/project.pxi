
#######################
# Project
#
cdef inline Project initProject(ProjectT& p):
    cdef Project project = Project()
    project.setProject(p)
    return project


cdef class Project:
    """
    A Project 

    :var ``id``: str 
    :var code: str 
    :var title: str 
    :var isActive: bool 
    """
    cdef ProjectT project

    cdef setProject(self, ProjectT& proj):
        self.project = proj

    property id:
        def __get__(self):
            cdef Guid id_ = self.project.id
            return id_

    property code:
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
        """
        Get the folders for this project 

        :returns: list[:class:`plow.Folder`]
        """
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
        """
        Set the active state of the Project 

        :param active: bool
        """
        set_project_active(self.id, active)
        self.project.isActive = active

cpdef get_project(Guid& guid):
    """
    Get a Project by id 

    :param guid: str - project id 
    :returns: :class:`plow.Project`
    """
    cdef: 
        ProjectT projT 
        Project project

    getClient().proxy().getProject(projT, guid)
    project = initProject(projT)
    return project


def get_project_by_code(string code):
    """
    Look up a Project by its code

    :param code: str 
    :returns: :class:`plow.Project`
    """
    cdef: 
        ProjectT projT 
        Project project

    getClient().proxy().getProjectByCode(projT, code)
    project = initProject(projT)
    return project


def get_projects():
    """
    Get a list of all Projects 

    :returns: list[:class:`plow.Project`]
    """
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
    """
    Return a list of only active Projects 

    :returns: list[:class:`plow.Project`]
    """
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
    """
    Create a new Project with a title and code 

    :param title: str - A full project title  
    :param code: str - A short code to indentify the project 
    :returns: :class:`plow.Project`
    """
    cdef ProjectT projT
    cdef Project proj 
    getClient().proxy().createProject(projT, title, code)
    proj = initProject(projT)
    return proj

cpdef inline set_project_active(Guid& id, bint active):
    """
    Set a project to be active 

    :param id: str - project id 
    :param active: bool 
    """
    getClient().proxy().setProjectActive(id, active)


 