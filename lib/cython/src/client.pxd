from RpcService cimport RpcServiceClient


cdef extern from "client.h" namespace "Plow":

	cdef cppclass PlowClient:
		RpcServiceClient proxy()

	cdef PlowClient* getClient() except +