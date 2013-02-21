from plow.rndaemon.server import get_server
from plow.rndaemon.rpc import RndServiceApi 

class ServiceHandler(object):

	def sendPing(self, ping):
		print "Got Ping:", ping


mock = get_server(RndServiceApi, ServiceHandler(), 9090)
mock.serve()