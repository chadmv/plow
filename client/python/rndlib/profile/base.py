
import platform
import socket
import logging

import rndlib.conf as conf
import rndlib.client as client

from rndlib.rpc import ttypes

logger = logging.getLogger(__name__)

class AbstractProfiler(object):
    def __init__(self):
        self.data = { "platform": platform.platform() }
        self.update()

        for key, value in self.data.iteritems():
            logger.info("%s = %s" % (key, value))

    def sendPing(self, tasks, isReboot=False):
        
        # Update the values (calls subclass impl)
        self.update()

        # Create the hardware profile
        hw = ttypes.Hardware()
        hw.physicalCpus = self.physicalCpus
        hw.logicalCpus = self.logicalCpus
        hw.totalRamMb = self.totalRamMb
        hw.freeRamMb = self.freeRamMb
        hw.totalSwapMb = self.totalSwapMb
        hw.freeSwapMb = self.freeSwapMb
        hw.cpuModel = self.cpuModel
        hw.platform = self.platform

        # Create a ping
        ping = ttypes.Ping()
        ping.hostname = socket.getfqdn()
        ping.ipAddr = socket.gethostbyname(socket.getfqdn())
        ping.isReboot = isReboot
        ping.bootTime = self.bootTime
        ping.hw = hw
        ping.tasks = tasks

        if conf.NETWORK_DISABLED:
            return

        logger.info("Sending ping: %s" % ping)
        try:
            conn = client.getPlowConnection()
            conn.sendPing(ping)
        except Exception, e:
            logger.warn("Unable to send ping to plow server, %s" % e)

    def update(self):
        pass

    def __getattr__(self, k):
        return self.data[k]

    def __str__(self):
        return str(self.data)
