import random

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

from rpc import RpcServiceApi

import plow.conf as conf

class Singleton(type):
    def __init__(cls, name, bases, dict):
        super(Singleton, cls).__init__(name, bases, dict)
        cls.instance = None 

    def __call__(cls, *args, **kw):
        if cls.instance is None:
            cls.instance = super(Singleton, cls).__call__(*args, **kw)
        return cls.instance

class PlowConnection(object):
    __metaclass__ = Singleton

    def __init__(self, hosts=None):
        self.__hosts = hosts
        if not self.__hosts:
            self.__hosts = conf.get("plow", "hosts").split(",")
        if not self.__hosts:
            self.__hosts = ["localhost:11336"]

        self.__socket = None
        self.__transport = None
        self.__protocol = None
        self.__service = None

        self.setup()

    def setup(self):
        host, port = self.__randomServer()
        self.__socket = TSocket.TSocket(host, port)
        self.__transport = TTransport.TFramedTransport(self.__socket)
        self.__protocol = TBinaryProtocol.TBinaryProtocol(self.__transport)

    def disconnect(self):
        self.__transport.close()

    @property
    def service(self):
        if not self.__service:
            self.__transport.open()
            self.__service = RpcServiceApi.Client(self.__protocol)
        return self.__service

    def __randomServer(self):
        rand = random.randint(0, len(self.__hosts)-1)
        host, port = self.__hosts[rand].split(":")
        return (host, int(port))


Conn = PlowConnection()






