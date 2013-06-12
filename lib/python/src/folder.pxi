
#######################
# Folders
#

cdef inline Folder initFolder(FolderT& f):
    cdef Folder folder = Folder()
    folder.setFolder(f)
    return folder


cdef class Folder(PlowBase):
    """
    A Folder 

    :var id: str 
    :var name: str 
    :var minCores: int
    :var maxCores: int
    :var runCores: int
    :var order: int
    :var totals: :class:`.TaskTotals`
    :var jobs: list[:class:`.Job`]
    
    """
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
        self.folder = f
        self._jobs = []
        self._totals = initTaskTotals(self.folder.totals)

    property id:
        def __get__(self): return self.folder.id

    property name:
        def __get__(self): return self.folder.name
        def __set__(self, string name):
            set_folder_name(self.id, name)
            self.folder.name = name

    property minCores:
        def __get__(self): return self.folder.minCores

    property maxCores:
        def __get__(self): return self.folder.maxCores

    property runCores:
        def __get__(self): return self.folder.runCores

    property order:
        def __get__(self): return self.folder.order

    property totals:
        def __get__(self): return self._totals

    property jobs:
        def __get__(self):
            cdef JobT jobT

            if not self._jobs:
                self._jobs = [initJob(jobT) for jobT in self.folder.jobs]

            return self._jobs

    @reconnecting
    def refresh(self):
        """
        Refresh the attributes from the server
        """
        cdef FolderT folder
        conn().proxy().getFolder(folder, self.folder.id)
        self.setFolder(folder)

    def set_min_cores(self, int value):
        """
        Set the minimum cores  

        :param value: int - cores 
        """        
        set_folder_min_cores(self, value)

    def set_max_cores(self, int value):
        """
        Set the maximum cores  

        :param value: int - cores 
        """     
        set_folder_max_cores(self, value)

    def delete(self):
        """
        Delete this Folder 
        """
        delete_folder(self)

    def get_procs(self):
        """
        Get current procs 

        :returns: list[:class:`.Proc`]
        """
        return get_procs(folderIds=[self.id])


@reconnecting
def get_folder(Guid& folderId):
    """
    Get a Folder by id 

    :param folderId: str 
    :returns: :class:`.Folder`
    """
    cdef:
        FolderT folderT
        Folder folder 

    conn().proxy().getFolder(folderT, folderId)
    folder = initFolder(folderT)
    return folder

def get_folders(Project project):
    """
    Get a list of Folders by project id 

    :param project: :class:`.Project`
    :returns: list[:class:`.Folder`]
    """
    cdef Project proj = get_project(project.id)
    cdef list folders = Project.get_folders(proj)
    return

def create_folder(Project project, string name):
    """
    Create a folder 

    :param project: :class:`.Project`
    :param name: str - folder name 
    :returns: :class:`.Folder`
    """
    cdef FolderT folderT 
    conn().proxy().createFolder(folderT, project.id, name)
    cdef Folder folder = initFolder(folderT)
    return folder

@reconnecting
def set_folder_min_cores(Folder folder, int value):
    """
    Set the minimum cores for a Folder 

    :param folder: :class:`.Folder`
    :param value: int - cores 
    """
    conn().proxy().setFolderMinCores(folder.id, value)

@reconnecting
def set_folder_max_cores(Folder folder, int value):
    """
    Set the maximum cores for a Folder 

    :param folder: :class:`.Folder`
    :param value: int - cores 
    """
    conn().proxy().setFolderMaxCores(folder.id, value)

@reconnecting
def set_folder_name(Folder folder, string& name):
    """
    Set the name for a Folder 

    :param folder: :class:`.Folder`
    :param name: str  
    """
    conn().proxy().setFolderName(folder.id, name)

@reconnecting
def delete_folder(Folder folder):
    """
    Delete a folder 

    :param folder: :class:`.Folder`
    """
    conn().proxy().deleteFolder(folder.id)


