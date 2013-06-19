
import functools 


EX_CONNECTION = set([
    "write() send(): Broken pipe",
    "Called write on non-open socket",
    "No more data to read.",
    "connect() failed: Connection refused"
    ])

# A decorator that will try to run the function.
# and catch a possible connection failure.
# It will then try to reconnect, and retry the
# original call.
def reconnecting(object func):

    @functools.wraps(func, ('__name__', '__doc__'), ('__dict__',))
    def wrapper(*args, **kwargs):
        try:
            return func(*args, **kwargs)

        except RuntimeError, e:
                
            LOGGER.exception("Error running %r", func)

            if str(e).strip() in EX_CONNECTION:
                # print "reconnecting!"
                LOGGER.debug("Connection lost. Retrying call")
                reconnect()
                try:
                    return func(*args, **kwargs)
                except Exception, e:
                    LOGGER.exception("Error re-running %r after reconnect", func)
                    raise
            else:
                raise PlowConnectionError(*e.args)

    cdef str attr
    for attr in ('__qualname__', '__module__', '__repr__'):
        try:
            setattr(wrapper, attr, getattr(func, attr))
        except AttributeError:
            pass

    return wrapper


cdef Attrs dict_to_attrs(dict d):
    """
    Convert a python dictionary to an Attrs object
    """
    cdef Attrs attrs
    for key, val in d.iteritems():
        attrs[str(key)] = str(val)

    return attrs