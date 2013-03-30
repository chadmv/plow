
#######################
# MatcherType
#

@cython.internal
cdef class _MatcherType:
    cdef:
        readonly int CONTAINS, NOT_CONTAINS, IS
        readonly int IS_NOT, BEGINS_WITH, ENDS_WITH

    def __cinit__(self):
        self.CONTAINS = MATCH_CONTAINS
        self.NOT_CONTAINS = MATCH_NOT_CONTAINS
        self.IS = MATCH_IS
        self.IS_NOT = MATCH_IS_NOT
        self.BEGINS_WITH = MATCH_BEGINS_WITH
        self.ENDS_WITH = MATCH_ENDS_WITH

MatcherType = _MatcherType()


#######################
# MatcherField
#

@cython.internal
cdef class _MatcherField:
    cdef readonly int JOB_NAME, PROJECT_CODE, USER, ATTR

    def __cinit__(self):
        self.JOB_NAME = MATCH_JOB_NAME
        self.PROJECT_CODE = MATCH_PROJECT_CODE
        self.USER = MATCH_USER
        self.ATTR = MATCH_ATTR

MatcherField = _MatcherField()



#######################
# Matcher
#

cdef inline Matcher initMatcher(MatcherT& m):
    cdef Matcher matcher = Matcher()
    matcher.setMatcher(m)
    return matcher


cdef class Matcher:

    cdef MatcherT matcher

    cdef setMatcher(self, MatcherT& m):
        self.matcher = m

    property id:
        def __get__(self): return self.matcher.id
        def __set__(self, Guid val): self.matcher.id = val

    property type:
        def __get__(self): return self.matcher.type
        def __set__(self, int val): self.matcher.type = <MatcherType_type>val

    property field:
        def __get__(self): return self.matcher.field
        def __set__(self, int val): self.matcher.field = <MatcherField_type>val

    property value:
        def __get__(self): return self.matcher.value
        def __set__(self, string val): self.matcher.value = val


 #######################
# ActionType
#

@cython.internal
cdef class _ActionType:
    cdef:
        readonly int SET_FOLDER, SET_MIN_CORES, SET_MAX_CORES
        readonly int PAUSE, STOP_PROCESSING

    def __cinit__(self):
        self.SET_FOLDER = ACTION_SET_FOLDER
        self.SET_MIN_CORES = ACTION_SET_MIN_CORES
        self.SET_MAX_CORES = ACTION_SET_MAX_CORES
        self.PAUSE = ACTION_PAUSE
        self.STOP_PROCESSING = ACTION_STOP_PROCESSING

ActionType = _ActionType()


#######################
# Action
#

cdef inline Action initAction(ActionT& a):
    cdef Action action = Action()
    action.setAction(a)
    return action


cdef class Action:

    cdef ActionT action

    cdef setAction(self, ActionT& a):
        self.action = a

    property id:
        def __get__(self): return self.action.id
        def __set__(self, Guid val): self.action.id = val

    property type:
        def __get__(self): return self.action.type
        def __set__(self, int val): self.action.type = <ActionType_type>val

    property value:
        def __get__(self): return self.action.value
        def __set__(self, string val): self.action.value = val


#######################
# Filter
#
cdef inline Filter initFilter(FilterT& a):
    cdef Filter filter = Filter()
    filter.setFilter(a)
    return filter


cdef class Filter:

    cdef FilterT _filter

    def __init__(self, **kwargs):
        self._filter.id = kwargs.get('id', '')
        self._filter.name = kwargs.get('name', '')
        self._filter.order = kwargs.get('order', 0)
        self._filter.enabled = kwargs.get('enabled', False)

        cdef Matcher m
        for m in kwargs.get('matchers', []):
            self._filter.matchers.push_back(m.matcher) 

        cdef Action a
        for a in kwargs.get('actions', []):
            self._filter.actions.push_back(a.action) 

    def __repr__(self):
        return "<Filter: %s>" % self.name
        
    cdef setFilter(self, FilterT& a):
        self._filter = a


