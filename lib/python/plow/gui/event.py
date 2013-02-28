

class EventManager(object):

    __Binds = { }

    @classmethod
    def bind(cls, name, function):
        funcs = cls.__Binds.get(name)
        if not funcs:
            funcs = [function]
            cls.__Binds[name] = funcs
        else:
            funcs.append(function)

    @classmethod
    def emit(cls, name, *args, **kwargs):
        funcs = cls.__Binds.get(name)
        if not funcs:
            return
        for func in funcs:
            func(*args, **kwargs)

