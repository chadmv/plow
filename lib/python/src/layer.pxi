


#######################
# Layers
#

cdef class LayerSpec:
    """
    Defines a new layer 

    :var name: str
    :var range: str 
    :var chunk: int 
    :var minCores: int 
    :var maxCores: int 
    :var minRamMb: int 
    :var threadable: bool
    :var command: list[str]
    :var depends: list[:class:`.DependSpec`] 
    :var tasks: list [:class:`.TaskSpec`]
    :var tags: set(str)

    """
    cdef public string name, range
    cdef public int chunk, minCores, maxCores, minRamMb
    cdef public bint threadable
    cdef list command, depends, tasks 
    cdef set tags
    cdef LayerSpecT__isset __isset

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

    property range:
        def __get__(self): return self.range
        def __set__(self, val): 
            self.range = val
            __isset.range = True

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
    """
    Represents an existing layer 

    :var id: str
    :var name: str
    :var range: str
    :var chunk: int
    :var minCores: int
    :var maxCores: int
    :var runCores: int
    :var minRamMb: int
    :var maxRssMb: int
    :var threadable: bool
    :var totals: list[:class:`.TaskTotals`]
    :var tags: set(str)
    
    """
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

    cpdef refresh(self):
        """
        Refresh the attributes from the server
        """
        getClient().proxy().getLayerById(self._layer, self._layer.id)

    def get_outputs(self):
        """ :returns: list[:class:`.Output`] """
        return get_layer_outputs(self)

    def add_output(self, string path, Attrs& attrs):
        """
        Add an output to the layer 

        :param path: str 
        :param attrs: dict
        """
        add_layer_output(self, path, attrs)

    def set_tags(self, c_set[string]& tags):
        """
        Set the tags for the layer 

        :param tags: set(str)
        """
        set_layer_tags(self, tags)
        self._layer.tags = tags

    def set_threadable(self, bint threadable):
        """ :param threadable: bool """
        set_layer_threadable(self, threadable)
        self._layer.threadable = threadable

    def set_min_cores_per_task(self, int minCores):
        """ :param minCores: int """
        set_layer_min_cores_per_task(self, minCores)
        self._layer.minCores = minCores

    def set_max_cores_per_task(self, int maxCores):
        """ :param maxCores: int """
        set_layer_max_cores_per_task(self, maxCores)
        self._layer.maxCores = maxCores

    def set_min_ram_per_task(self, int minRam):
        """ :param minRam: int """
        set_layer_min_ram_per_task(self, minRam)
        self._layer.minRamMb = minRam

cpdef inline get_layer_by_id(Guid& layerId):
    """
    Get a layer by its id 

    :param layerId: str 
    :returns: :class:`.Layer`
    """
    cdef:
        LayerT layerT 
        Layer layer

    try:
        getClient().proxy().getLayerById(layerT, layerId)
    except RuntimeError:
        return None 

    layer = initLayer(layerT)
    return layer

def get_layer(Job job, string name):
    """
    Get layer by its name

    :param job: :class:`.Job`
    :param name: str 
    :returns: :class:`.Layer`
    """
    cdef:
        LayerT layerT 
        Layer layer

    try:
        getClient().proxy().getLayer(layerT, job.id, name)
    except RuntimeError:
        return None 

    layer = initLayer(layerT)
    return layer

def get_layers(Job job):
    """
    Get layers by a job id 

    :param job: :class:`.Job`
    :returns: list[:class:`.Layer`]
    """
    cdef:
        LayerT layerT 
        Layer layer
        vector[LayerT] layers 
        list ret

    try:
        getClient().proxy().getLayers(layers, job.id)
    except RuntimeError:
        ret = []
        return ret

    ret = [initLayer(layerT) for layerT in layers]
    return ret

cpdef inline add_layer_output(Layer layer, string path, Attrs& attrs):
    """
    A an output to a layer 

    :param layer: :class:`.Layer`
    :param path: str 
    :param attrs: dict
    """
    getClient().proxy().addOutput(layer.id, path, attrs)

cpdef inline get_layer_outputs(Layer layer):
    """
    Get the outputs for a layer 

    :param layer: :class:`.Layer`
    :returns: list[:class:`.Layer`]
    """
    cdef:
        vector[OutputT] outputs
        OutputT outT 
        Output output 
        list ret 

    try:
        getClient().proxy().getLayerOutputs(outputs, layer.id)
    except RuntimeError:
        ret = []
        return ret 

    ret = [initOutput(outT) for outT in outputs]
    return ret

cpdef inline set_layer_tags(Layer layer, c_set[string]& tags):
    """ 
    :param layer: :class:`.Layer`
    :param tags: set(str) 
    """
    getClient().proxy().setLayerTags(layer.id, tags)

cpdef inline set_layer_min_cores_per_task(Layer layer, int minCores):
    """ 
    :param layer: :class:`.Layer`
    :param minCores: int 
    """
    getClient().proxy().setLayerMinRamPerTask(layer.id, minCores)

cpdef inline set_layer_max_cores_per_task(Layer layer, int maxCores):
    """ 
    :param layer: :class:`.Layer`
    :param maxCores: int 
    """
    getClient().proxy().setLayerMaxCoresPerTask(layer.id, maxCores)

cpdef inline set_layer_min_ram_per_task(Layer layer, int minRam):
    """ 
    :param layer: :class:`.Layer`
    :param minRam: int 
    """
    getClient().proxy().setLayerMinRamPerTask(layer.id, minRam)

cpdef inline set_layer_threadable(Layer layer, bint threadable):
    """ 
    :param layer: :class:`.Layer`
    :param threadable: bool
    """
    getClient().proxy().setLayerThreadable(layer.id, threadable)


