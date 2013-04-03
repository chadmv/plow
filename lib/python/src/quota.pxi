

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

cdef inline Quota initQuota(QuotaT& q):
    cdef Quota quota = Quota()
    quota.setQuota(q)
    return quota


cdef class Quota:
    """
    TODO

    :var id: str
    :var clusterId: str :class:`.Cluster` id
    :var projectId: str :class:`.Project` id 
    :var name: str 
    :var isLocked: bool
    :var size: int 
    :var burst: int 
    :var runCores: int

    """
    cdef QuotaT _quota

    cdef setQuota(self, QuotaT& q):
        self._quota = q

    property id:
        def __get__(self): return self._quota.id

    property clusterId:
        def __get__(self): return self._quota.clusterId

    property projectId:
        def __get__(self): return self._quota.projectId

    property name:
        def __get__(self): return self._quota.name

    property isLocked:
        def __get__(self): return self._quota.isLocked

    property size:
        def __get__(self): return self._quota.size

    property burst:
        def __get__(self): return self._quota.burst

    property runCores:
        def __get__(self): return self._quota.runCores

    def set_size(self, int size):
        """ :param size: int """
        set_quota_size(self.id, size)
        self._quota.size = size

    def set_burst(self, int burst):
        """ :param burst: int """
        set_quota_burst(self.id, burst)
        self._quota.burst = burst

    def set_locked(self, bint locked):
        """
        Set the lock state

        :param locked: bool
        """
        set_quota_locked(self.id, locked)
        self._quota.isLocked = locked


def get_quota(Guid& id):
    """
    Get a quota by id 

    :param id: :class:`.Quota` id 
    :returns: :class:`.Quota`
    """
    cdef: 
        QuotaT qT
        Quota q 

    getClient().proxy().getQuota(qT, id)
    q = initQuota(qT)
    return q

def get_quotas(**kwargs):
    """
    Get quotas matching various keyword filter params

    :param project: list[str :class:`.Project` id]
    :param cluster: list[str :class:`.Cluster` id]
    :returns: list[:class:`.Quota`]
    """
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
    """
    Create a quota for a project and cluster 

    :param projectId: The target :class:`.Project` id 
    :param clusterId: The target :class:`.Cluster` id 
    :param size: int 
    :param burst: int 
    :return: :class:`.Quota`
    """
    cdef:
        QuotaT qT
        Quota q 

    getClient().proxy().createQuota(qT, projectId, clusterId, size, burst)
    q = initQuota(qT)
    return q


cpdef inline set_quota_size( Guid& id, int size):
    """
    Set the quota size 

    :param id: :class:`.Quota` id 
    :param size: int 
    """
    getClient().proxy().setQuotaSize(id, size)

cpdef inline set_quota_burst( Guid& id, int burst):
    """
    Set the quota burst 

    :param id: :class:`.Quota` id 
    :param burst: int 
    """
    getClient().proxy().setQuotaBurst(id, burst)

cpdef inline set_quota_locked( Guid& id, bint locked):
    """
    Set the lock state of the quota

    :param id: :class:`.Quota` id 
    :param locked: bool 
    """
    getClient().proxy().setQuotaLocked(id, locked)

