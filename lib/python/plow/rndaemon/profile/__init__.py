import platform

__platform = platform.system()

if __platform == 'Darwin':
    from .macosx import SystemProfiler 

elif __platform == 'Linux':
    from .linux import SystemProfiler

else:
    raise NotImplementedError('platform %s is not supported' % __platform)

__all__ = ['SystemProfiler']
