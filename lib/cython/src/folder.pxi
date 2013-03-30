
#######################
# Folders
#

cdef inline Folder initFolder(FolderT& f):
    cdef Folder folder = Folder()
    folder.setFolder(f)
    return folder


cdef class Folder:
    """
    A Folder 

    :var id: str 
    :var name: str 
    :var minCores: int
    :var maxCores: int
    :var runCores: int
    :var order: int
    :var totals: :class:`plow.TaskTotals`
    :var jobs: list[:class:`plow.Job`]
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
        def __set__(self, string name):
            set_folder_name(self.id, name)
            self.folder.name = name

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

            if self._totals is None:
                totals = self.folder.totals
                result = initTaskTotals(totals)
                self._totals = result

            return self._totals

    property jobs:
        def __get__(self):
            cdef JobT jobT

            if not self._jobs:
                self._jobs = [initJob(jobT) for jobT in self.folder.jobs]

            return self._jobs

    def set_min_cores(self, int value):
        """
        Set the minimum cores  

        :param value: int - cores 
        """        
        set_folder_min_cores(self.id, value)

    def set_max_cores(self, int value):
        """
        Set the maximum cores  

        :param value: int - cores 
        """     
        set_folder_max_cores(self.id, value)

    def delete(self):
        """
        Delete this Folder 
        """
        delete_folder(self.id)


def get_folder(Guid& folderId):
    """
    Get a Folder by id 

    :param folderId: str 
    :returns: :class:`plow.Folder`
    """
    cdef:
        FolderT folderT
        Folder folder 

    getClient().proxy().getFolder(folderT, folderId)
    folder = initFolder(folderT)
    return folder

def get_folders(Guid& projectId):
    """
    Get a list of Folders by project id 

    :param projectId: :class:`plow.Project`.id  
    :returns: list[:class:`plow.Folder`]
    """
    cdef Project proj = get_project(projectId)
    cdef list folders = Project.get_folders(proj)
    return

def create_folder(Guid& projectId, string name):
    """
    Create a folder 

    :param projectId: :class:`plow.Project`.id 
    :param name: str - folder name 
    :returns: :class:`plow.Folder`
    """
    cdef FolderT folderT 
    getClient().proxy().createFolder(folderT, projectId, name)
    cdef Folder folder = initFolder(folderT)
    return folder

def get_job_board(Guid& projectId):
    """
    TODO
    """
    cdef: 
        FolderT folderT 
        vector[FolderT] folders

    getClient().proxy().getJobBoard(folders, projectId)
    cdef list ret = [initFolder(folderT) for folderT in folders]
    return ret

cpdef inline set_folder_min_cores(Guid& id, int value):
    """
    Set the minimum cores for a Folder 

    :param id: :class:`plow.Folder`.id 
    :param value: int - cores 
    """
    getClient().proxy().setFolderMinCores(id, value)

cpdef inline set_folder_max_cores(Guid& id, int value):
    """
    Set the maximum cores for a Folder 

    :param id: :class:`plow.Folder`.id 
    :param value: int - cores 
    """
    getClient().proxy().setFolderMaxCores(id, value)

cpdef inline set_folder_name(Guid& id, string& name):
    """
    Set the name for a Folder 

    :param id: :class:`plow.Folder`.id 
    :param name: str  
    """
    getClient().proxy().setFolderName(id, name)

cpdef inline delete_folder(Guid& id):
    """
    Delete a folder 

    :param id: :class:`plow.Folder`.id
    """
    getClient().proxy().deleteFolder(id)


