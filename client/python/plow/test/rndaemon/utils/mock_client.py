from plow.rndaemon.client import getPlowConnection
from plow.rndaemon.rpc.ttypes import Ping, Hardware  

import time

ping = Ping()
ping.hw = Hardware()

service, transport = getPlowConnection('localhost', 9090)
service.sendPing(ping)
time.sleep(1)

transport.close()
