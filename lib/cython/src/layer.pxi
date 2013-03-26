


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
        s.tags = self.tags

        cdef: 
            DependSpecT dSpecT
            DependSpec dSpec
        for dSpec in self.depends:
            dSpecT = dSpec.toDependSpecT()
            s.depends.push_back(dSpecT) 

        cdef: 
            TaskSpecT tSpecT
            TaskSpec tSpec
        for tSpec in self.tasks:
            tSpecT = tSpec.toTaskSpecT()
            s.tasks.push_back(tSpecT) 

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


cdef inline Job initLayer(LayerT& l):
    cdef Layer layer = Layer()
    layer.setLayer(l)
    return layer


cdef class Layer:

    cdef:
        LayerT _layer 
        TaskTotals _totals

    def __init__(self):
        self._totals = None

    def __repr__(self):
        return "<Layer: %s>" % self.name

    cdef setLayer(self, LayerT& l):
        cdef TaskTotalsT totals = self._layer.totals
        self._layer = l
        self._totals = initTaskTotals(totals)

    property id:
        def __get__(self): return self._layer.id

    property name:
        def __get__(self): return self._layer.name

    property range:
        def __get__(self): return self._layer.range

    property chunk:
        def __get__(self): return self._layer.chunk

    property minCores:
        def __get__(self): return self._layer.minCores

    property maxCores:
        def __get__(self): return self._layer.maxCores

    property runCores:
        def __get__(self): return self._layer.runCores

    property minRamMb:
        def __get__(self): return self._layer.minRamMb

    property maxRssMb:
        def __get__(self): return self._layer.maxRssMb

    property threadable:
        def __get__(self): return self._layer.threadable

    property totals:
        def __get__(self):
            cdef TaskTotalsT totals

            if not self._totals:
                totals = self._layer.totals
                result = initTaskTotals(totals)
                self._totals = result

            return self._totals

    property tags:
        def __get__(self): return self._layer.tags

    def get_outputs(self):
        return get_layer_outputs(self.id)

    def add_output(self, string path, Attrs& attrs):
        add_output(self.id, path, attrs)

    def set_tags(self, c_set[string]& tags):
        set_layer_tags(self.id, tags)
        self._layer.tags = tags

    def set_threadable(self, bint threadable):
        set_layer_threadable(self.id, threadable)
        self._layer.threadable = threadable

    def set_min_cores_per_task(self, int minCores):
        set_layer_min_cores_per_task(self.id, minCores)
        self._layer.minCores = minCores

    def set_max_cores_per_task(self, int maxCores):
        set_layer_max_cores_per_task(self.id, maxCores)
        self._layer.maxCores = maxCores

    def set_min_ram_per_task(self, int minRam):
        set_layer_min_ram_per_task(self.id, minRam)
        self._layer.minRamMb = minRam

def get_layer_by_id(Guid& layerId):
    cdef:
        LayerT layerT 
        Layer layer

    try:
        getClient().proxy().getLayerById(layerT, layerId)
    except RuntimeError:
        return None 

    layer = initLayer(layerT)
    return layer

def get_layer(Guid& jobId, string name):
    cdef:
        LayerT layerT 
        Layer layer

    try:
        getClient().proxy().getLayerById(layerT, name)
    except RuntimeError:
        return None 

    layer = initLayer(layerT)
    return layer

def get_layers(Guid& jobId):
    cdef:
        LayerT layerT 
        Layer layer
        vector[LayerT] layers 
        list ret

    try:
        getClient().proxy().getLayers(layers, jobId)
    except RuntimeError:
        ret = []
        return ret

    ret = [initLayer(layerT) for layerT in layers]
    return ret

cpdef inline add_output(Guid& layerId, string path, Attrs& attrs):
    getClient().proxy().addOutput(layerId, path, attrs)

cpdef inline list get_layer_outputs(Guid& layerId):
    cdef:
        vector[OutputT] outputs
        OutputT outT 
        Output output 
        list ret 

    try:
        getClient().proxy().getLayerOutputs(outputs, layerId)
    except RuntimeError:
        ret = []
        return ret 

    ret = [initOutput(outT) for outT in outputs]
    return ret

cpdef inline set_layer_tags(Guid& guid, c_set[string]& tags):
    getClient().proxy().setLayerTags(guid, tags)

cpdef inline set_layer_min_cores_per_task(Guid& guid, int minCores):
    getClient().proxy().setLayerMinRamPerTask(guid, minCores)

cpdef inline set_layer_max_cores_per_task(Guid& guid, int maxCores):
    getClient().proxy().setLayerMaxCoresPerTask(guid, maxCores)

cpdef inline set_layer_min_ram_per_task(Guid& guid, int minRam):
    getClient().proxy().setLayerMinRamPerTask(guid, minRam)

cpdef inline set_layer_threadable(Guid& guid, bint threadable):
    getClient().proxy().setLayerThreadable(guid, threadable)


