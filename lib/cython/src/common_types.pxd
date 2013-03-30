from libcpp.string cimport string 
from libcpp.map cimport map 

cdef extern from "rpc/common_types.h" namespace "Plow":
    ctypedef string Guid
    ctypedef long Timestamp
    ctypedef map[string, string] Attrs
