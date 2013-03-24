


#######################
# Layers
#

cdef class LayerSpec:

    cdef public string name, range
    cdef public int chunk, minCores, maxCores, minRamMb
    cdef public bint threadable
    cdef list command, depends, tasks 
    cdef set tags

    def __init__(self, **kwargs):
        self.name = kwargs.get('name', '')
        self.range = kwargs.get('range', '')
        self.chunk = kwargs.get('chunk', 0)
        self.minCores = kwargs.get('minCores', 0)
        self.maxCores = kwargs.get('maxCores', 0)
        self.minRamMb = kwargs.get('minRamMb', 0)
        self.threadable = kwargs.get('threadable', False) 
        self.command = kwargs.get('command', [])
        self.depends = kwargs.get('depends', [])
        self.tasks = kwargs.get('tasks', [])
        self.tags = kwargs.get('tags', set())

    def __repr__(self):
        return "<LayerSpec: %s>" % self.name

    cdef LayerSpecT toLayerSpecT(self):
        cdef LayerSpecT s

        s.name = self.name 
        s.range = self.range
        s.chunk = self.chunk
        s.minCores = self.minCores
        s.maxCores = self.maxCores
        s.minRamMb = self.minRamMb
        s.threadable = self.threadable
        s.command = self.command

        # s.depends = self.depends
        # s.tasks = self.tasks 

        s.tags = self.tags

        return s

    property command:
        def __get__(self): return self.command
        def __set__(self, val): self.command = val

    property depends:
        def __get__(self): return self.depends
        def __set__(self, val): self.depends = val

    property tasks:
        def __get__(self): return self.tasks
        def __set__(self, val): self.tasks = val

    property tags:
        def __get__(self): return self.tags
        def __set__(self, val): self.tags = val

# def get_layer_by_id(Guid& layerId):
#     pass

# def get_layer(Guid& jobId, string name):
#     pass

# def getLayers(Guid& jobId):
#     pass

# def add_layer_output(Guid& layerId, string path, Attrs& attrs):
#     pass

# def get_layer_outputs(Guid& layerId):
#     pass


