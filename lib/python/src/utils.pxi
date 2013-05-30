
import functools 

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

            if str(e) in EX_CONNECTION:
                # print "reconnecting!"
                LOGGER.debug("Connection lost. Retrying call")
                reconnect()
                try:
                    return func(*args, **kwargs)
                except Exception, e:
                    LOGGER.exception("Error re-running %r after reconnect", func)
                    raise
            else:
                raise e

    cdef str attr
    for attr in ('__qualname__', '__module__', '__repr__'):
        try:
            setattr(wrapper, attr, getattr(func, attr))
        except AttributeError:
            pass

    return wrapper

