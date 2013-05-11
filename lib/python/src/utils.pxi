
# A decorator that will try to run the function.
# and catch a possible connection failure.
# It will then try to reconnect, and retry the
# original call.

def reconnecting(object func):

    def wrapper(*args, **kwargs):
        try:
            return func(*args, **kwargs)

        except RuntimeError, e:
            
            if str(e) in EX_CONNECTION:
                LOGGER.debug("Connection lost. Retrying call")
                reconnect()
                return func(*args, **kwargs)
            
            else:
                raise

    wrapper.__doc__ = func.__doc__
    return wrapper


# class __wrapper(object):

#     def __init__(self, func):
#         self.func = func
#         self.__doc__ = func.__doc__

#     def __repr__(self):
#         return self.func.__repr__()

#     def __call__(self, *args, **kwargs):
#         try:
#             return self.func(*args, **kwargs)

#         except RuntimeError, e:
            
#             if str(e) in EX_CONNECTION:
#                 LOGGER.debug("Connection lost. Retrying call")
#                 reconnect()
#                 return self.func(*args, **kwargs)
            
#             else:
#                 raise

# def reconnecting(object func):
#     return __wrapper(func)
