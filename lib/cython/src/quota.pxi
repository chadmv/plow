

#######################
# QuotaFilter
#

@cython.internal
cdef class QuotaFilter:

    cdef QuotaFilterT value

    def __init__(self, **kwargs):
        self.value.project = kwargs.get('project', [])
        self.value.cluster = kwargs.get('cluster', [])


#######################
# Quote
#

cdef inline Quota initQuota(Quota& q):
    cdef Quota quota = Quota()
    quota.setQuota(q)
    return quota

"""
    cdef cppclass QuotaT:
        Guid id
        Guid clusterId
        Guid projectId
        string clusterName
        string projectName
        bint locked
        int totalCores
        int burstCores
        int freeCores
        """
cdef class Quota:

    cdef Quota _quota

    cdef setQuota(self, Quota& q):
        self._quota = q

    property id:
        def __get__(self): return self._quota.id

    property clusterId:
        def __get__(self): return self._quota.clusterId

    property projectId:
        def __get__(self): return self._quota.projectId

    property projectName:
        def __get__(self): return self._quota.projectName

    property clusterName:
        def __get__(self): return self._quota.clusterName

    property locked:
        def __get__(self): return self._quota.locked

    property totalCores:
        def __get__(self): return self._quota.totalCores

    property burstCores:
        def __get__(self): return self._quota.burstCores

    property freeCores:
        def __get__(self): return self._quota.freeCores

    def set_size(self, int size):
        set_quota_size(self.id, size)
        self._quota.size = size

    def set_burst(self, int burst):
        set_quota_burst(self.id, burst)
        self._quota.burst = burst

    def set_locked(self, bint locked):
        set_quota_locked(self.id, locked)
        self._quota.locked = locked


def get_quota(Guid& id):
    cdef: 
        QuotaT qT
        Quota q 

    getClient().proxy().getQuota(qT)
    q = initQuota(qT)
    return q

def getQuotas(**kwargs):
    cdef:
        QuotaT qT
        vector[QuotaT] quotas 
        list ret
        QuotaFilter filter = QuotaFilter(**kwargs)
        QuotaFilterT f = filter.value

    getClient().proxy().getQuotas(quotas, f)
    ret = [initQuota(qT) for qT in quotas]
    return ret

def create_quota(Guid& projectId,  Guid& clusterId, int size, int burst):
    cdef:
        QuotaT qT
        Quota q 

    getClient().proxy().createQuota(qT, projectId, clusterId, size, burst)
    q = initQuota(qT)
    return q


cpdef inline set_quota_size( Guid& id, int size):
    getClient().proxy().setQuotaSize(id, size)

cpdef inline set_quota_burst( Guid& id, int burst):
    getClient().proxy().setQuotaBurst(id, burst)

cpdef inline set_quota_locked( Guid& id, bint locked):
    getClient().proxy().setQuotaLocked(id, locked)

