
#######################
# Output
#

cdef Output initOutput(OutputT& o):
    cdef Output out = Output()
    out.setOutput(o)
    return out


cdef class Output:
    """
    Represents an output of a :class:`.Layer`

    :var outputId: str 
    :var path: str path 
    :var attrs: dict attributes 
    
    """
    cdef readonly string path, outputId 
    cdef dict attrs

    def __init__(self):
        self.outputId = ''
        self.path = ''
        self.attrs = {} 

    def __repr__(self):
        return "<Output: %s >" % self.path

    cdef setOutput(self, OutputT& o):
        self.outputId = o.outputId
        self.path = o.path
        self.attrs = o.attrs

    property attrs:
        def __get__(self): return self.attrs
        def __set__(self, dict d): self.attrs = d

    def update(self):
        """
        Commit any changes to the attrs dict
        """
        if self.outputId.empty():
            raise ValueError("Output has not been created yet. The outputId is empty")

        cdef Attrs attrs = dict_to_attrs(self.attrs)
        set_output_attrs(self.outputId, attrs)
        self.attrs = attrs


@reconnecting
def update_output_attrs(Guid& outputId, dict attrs):
    """
    Update the dict attributes of an existing Output

    :param outputId: str :class:`.Output` id
    :param attrs: dict
    """

    conn().proxy().updateOutputAttrs(outputId, dict_to_attrs(attrs))

@reconnecting
def set_output_attrs(Guid& outputId, dict attrs):
    """
    Replace the dict attributes of an existing Output

    :param outputId: str :class:`.Output` id
    :param attrs: dict
    """
    conn().proxy().setOutputAttrs(outputId, dict_to_attrs(attrs))    

@reconnecting
def get_output_attrs(Guid& outputId):
    """
    Get the dict attributes of an existing Output

    :param outputId: str :class:`.Output` id
    :returns: dict
    """
    cdef Attrs attrs
    conn().proxy().getOutputAttrs(attrs, outputId)
    return attrs 

