
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
    cdef readonly int JOB_NAME, USER, ATTR

    def __cinit__(self):
        self.JOB_NAME = MATCH_JOB_NAME
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
    """
    A matcher is a combination of a type 
    a field and a value

    :var id: str 
    :var type: :data:`.MatcherType`
    :var field: :data:`.MatcherField`
    :var value: str

    """
    cdef MatcherT matcher

    cdef setMatcher(self, MatcherT& m):
        self.matcher = m

    property id:
        def __get__(self): return self.matcher.id

    property type:
        def __get__(self): return self.matcher.type

    property field:
        def __get__(self): return self.matcher.field

    property value:
        def __get__(self): return self.matcher.value

    property attr:
        def __get__(self): return self.matcher.attr

    @reconnecting
    def refresh(self):
        """
        Refresh the attributes from the server
        """
        cdef MatcherT matcher 
        conn().proxy().getMatcher(matcher, self.matcher.id)
        self.setMatcher(matcher)

    def delete(self):
        """ Delete the matcher  """
        delete_matcher(self)


def create_field_matcher(Filter filter, int field, int typ, string& value):
    """
    Create a field Matcher 

    :param filter: :class:`.Filter`
    :param field: :data:`.MatcherField` value
    :param typ: :data:`.MatcherType` value 
    :param value: str value for the matcher
    :returns: :class:`.Matcher`
    """
    cdef:
        MatcherT matcher 
        Matcher ret 

    conn().proxy().createFieldMatcher(matcher, 
                                           filter.id, 
                                           <MatcherField_type>field, 
                                           <MatcherType_type>typ, 
                                           value )
    ret = initMatcher(matcher)
    return ret

def create_attr_matcher(Filter filter, int typ, string& attr, string& value):
    """
    Create an attribute Matcher 

    :param filter: :class:`.Filter`
    :param typ: :data:`.MatcherType` value 
    :param attr: str attr for the matcher
    :param value: str value for the matcher
    :returns: :class:`.Matcher`
    """
    cdef:
        MatcherT matcher 
        Matcher ret 

    conn().proxy().createAttrMatcher(matcher, 
                                          filter.id, 
                                          <MatcherType_type>typ, 
                                          attr,
                                          value )
    ret = initMatcher(matcher)
    return ret

@reconnecting
def get_matcher(Guid& matcherId):
    """
    Get a matcher by id

    :param id: str :class:`.Filter` id
    :returns: :class:`.Matcher`
    """
    cdef:
        MatcherT matcher 
        Matcher ret 

    conn().proxy().getMatcher(matcher, matcherId)
    ret = initMatcher(matcher)
    return ret

@reconnecting
def get_matchers(Filter filter):
    """
    Get a list of Matchers by a filter

    :param filter: :class:`.Filter`
    :returns: list[:class:`.Matcher`]
    """
    cdef:
        MatcherT m
        vector[MatcherT] matchers
        list ret

    conn().proxy().getMatchers(matchers, filter.id)
    ret = [initMatcher(m) for m in matchers]
    return ret        

@reconnecting
def delete_matcher(Matcher matcher):
    """
    Delete a Matcher 

    :param matcher: :class:`.Matcher`
    """
    conn().proxy().deleteMatcher(matcher.id)


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
    """
    An Action is represents a type and a value 

    :var id: str 
    :var type: :data:`.ActionType`
    :var value: str

    """
    cdef ActionT action

    cdef setAction(self, ActionT& a):
        self.action = a

    property id:
        def __get__(self): return self.action.id

    property type:
        def __get__(self): return self.action.type

    property value:
        def __get__(self): return self.action.value

    @reconnecting
    def refresh(self):
        """
        Refresh the attributes from the server
        """
        cdef ActionT action
        conn().proxy().getAction(action, self.action.id)
        self.setAction(action)

    def delete(self):
        """Delete the action"""
        delete_action(self)

def create_action(Filter filter, int typ, string& value):
    """
    Create an action 

    :param filter: :class:`.Filter`
    :param typ: :data:`.ActionType` value
    :param value: str 
    :returns: :class:`.Action`
    """
    cdef:
        ActionT action 
        Action ret 

    conn().proxy().createAction(action, filter.id, <ActionType_type>typ, value)
    ret = initAction(action)
    return ret

@reconnecting
def get_action(Guid& actionId):
    """
    Get an action by id 

    :param actionId: str :class:`.Action` id
    :returns: :class:`.Action`
    """
    cdef:
        ActionT action 
        Action ret 

    conn().proxy().getAction(action, actionId)
    ret = initAction(action)
    return ret

@reconnecting
def get_actions(Filter filter):
    """
    Get a list of actions from a filter 

    :param filter: :class:`.Filter` 
    :returns: list[:class:`.Action`]
    """    
    cdef:
        ActionT a
        vector[ActionT] actions
        list ret

    conn().proxy().getActions(actions, filter.id)
    ret = [initAction(a) for a in actions]
    return ret        

@reconnecting
def delete_action(Action action):
    """
    Delete an action 

    :param action: :class:`.Action`
    """
    conn().proxy().deleteAction(action.id)


#######################
# Filter
#
cdef inline Filter initFilter(FilterT& a):
    cdef Filter filter = Filter()
    filter.setFilter(a)
    return filter


cdef class Filter:
    """
    A filter combines matcher and actions instances

    :var id: str 
    :var name: str
    :var order: int 
    :var enabled: bool
    :var matchers: list[:class:`.Matcher`]
    :var actions: list[:class:`.Action`]

    """
    cdef:
        FilterT _filter
        list _matchers, _actions

    def __init__(self, **kwargs):
        self._filter.id = kwargs.get('id', '')
        self._filter.name = kwargs.get('name', '')
        self._filter.order = kwargs.get('order', 0)
        self._filter.enabled = kwargs.get('enabled', False)

        self._actions = []
        self._matchers = []

    def __repr__(self):
        return "<Filter: %s>" % self.name
        
    cdef setFilter(self, FilterT& a):
        self._filter = a
        self._actions = []
        self._matchers = []

    property id:
        def __get__(self): return self._filter.id

    property name:
        def __get__(self): return self._filter.name

    property order:
        def __get__(self): return self._filter.order

    property enabled:
        def __get__(self): return self._filter.enabled

    property matchers:
        def __get__(self): 
            cdef MatcherT m
            if not self._matchers:
                self._matchers = [initMatcher(m) for m in self._filter.matchers]
            return self._matchers

    property actions:
        def __get__(self): 
            cdef ActionT a 
            if not self._actions:
                self._actions = [initAction(a) for a in self._filter.actions]
            return self._actions

    @reconnecting
    def refresh(self):
        """
        Refresh the attributes from the server
        """
        cdef FilterT filt
        conn().proxy().getFilter(filt, self._filter.id)
        self.setFilter(filt)

    def delete(self):
        """
        Delete this filter 
        """
        delete_filter(self)

    def set_name(self, string& name):
        """
        Set the filter name 

        :param name: str 
        """
        set_filter_name(self, name)
        self._action.name = name

    def set_order(self, int order):
        """
        Set the order 

        :param order: int 
        """
        set_filter_order(self, order)
        self._action.order = order

    def increase_order(self):
        """ Increase the order """
        increase_filter_order(self)
        self.refresh()

    def decrease_filter_order(self):
        """ Decrease the order """
        decrease_filter_order(self)
        self.refresh()

def create_filter(Project project, string& name):
    """
    Create a filter for a project 

    :param project: :class:`.Project`
    :param name: str
    :returns: :class:`.Filter`
    """
    cdef:
        FilterT filterT
        Filter ret 

    conn().proxy().createFilter(filterT, project.id, name)
    ret = initFilter(filterT)
    return ret

@reconnecting
def get_filters(Project project):
    """
    Get a list of filters for a project 

    :param project: :class:`.Project`
    :returns: list[:class:`.Filter`]
    """
    cdef:
        FilterT f
        vector[FilterT] filters 
        list ret 

    conn().proxy().getFilters(filters, project.id)
    ret = [initFilter(f) for f in filters]
    return ret

@reconnecting
def get_filter(Guid& filterId):
    """
    Get a filter by id 

    :param filterId: str :class:`.Filter`.id
    :returns: :class:`.Filter`
    """
    cdef:
        FilterT filt 
        Filter ret 

    conn().proxy().getFilter(filt, filterId)
    ret = initFilter(filt)
    return ret

@reconnecting
def delete_filter(Filter filt):
    """
    Delete a filter

    :param filt: :class:`.Filter`
    """
    conn().proxy().deleteFilter(filt.id)

@reconnecting
def set_filter_name(Filter filt, string& name):
    """
    Set a filter name 

    :param filt: :class:`.Filter`
    :param name: str 
    """    
    conn().proxy().setFilterName(filt.id, name)
    filt.name = name

@reconnecting
def set_filter_order(Filter filt, int order):
    """
    Set the filter order

    :param filt: :class:`.Filter`
    :param order: int
    """    
    conn().proxy().setFilterOrder(filt.id, order)

def increase_filter_order(Filter filt):
    """
    Increase the filter order

    :param filt: :class:`.Filter`
    """    
    conn().proxy().increaseFilterOrder(filt.id)

def decrease_filter_order(Filter filt):
    """
    Decrease the filter order

    :param filt: :class:`.Filter`
    """    
    conn().proxy().decreaseFilterOrder(filt.id) 

