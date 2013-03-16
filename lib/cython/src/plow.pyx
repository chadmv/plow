cimport cython
cimport client

#######################
# Client
#

@cython.internal
cdef class PlowClient:
    
    cdef client.PlowClient* ptr

    def __cinit__(self):
        pass

    def __init__(self):
        pass   

    cdef setClientPtr(self, client.PlowClient* ptr):
        self.ptr = ptr

    def proxy(self):
        return 0


cpdef getClient():
    cdef:
        client.PlowClient* ptr
        PlowClient aClient 

    ptr = client.getClient()
    aClient = PlowClient()
    aClient.setClientPtr(ptr)

    return aClient


# @cython.internal
# cdef class Connection:
    
#   def __init__(self):
#       pass

#   def connect(self):
#       pass

#   def disconnect(self):
#       pass

#   def proxy(self):
#       pass



#
#######################
