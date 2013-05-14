"""
System profile plugin for testing purposes.
"""
import logging
import random 
import string
import platform
import socket
import time
from datetime import datetime, timedelta

from .base import AbstractProfiler

from ..rpc import ttypes
from .. import conf, client

logger = logging.getLogger(__name__)

__all__ = ["SystemProfiler"]


class SystemProfiler(AbstractProfiler):

    def __init__(self):
        rand = ''.join(random.choice(string.ascii_letters+string.digits+'-_') for ch in xrange(6))
        host = "{0}.{1}".format(socket.getfqdn(), rand)
        self.__hostname = host

        uptime = datetime.now() - timedelta(hours=random.randint(1,250))
        bootTime = int(time.mktime(uptime.timetuple()))

        self.data = {
            "platform": platform.platform(),
            "cpuModel": "Intel(R) Xeon(R) CPU X5472 @ 3.00GHz",
            "bootTime": bootTime,
        }

        self.data["physicalCpus"] = random.randint(1,8)
        self.data["logicalCpus"] = random.randint(1,2) * self.data["physicalCpus"]

        ram = random.choice([2048,4096,8192,16384])
        self.data["totalRamMb"] = ram
        self.data["totalSwapMb"] = random.choice(map(int, [ram*.25, ram*.5, ram, ram*2]))

        self.update()

        logger.info("starting Test SystemProfiler as host: %s", host)

    def reboot(self):
        logger.info("reboot requested")

    def getSubprocessOpts(self, cmd, **kwargs):
        cmd, opts = super(SystemProfiler, self).getSubprocessOpts(cmd, **kwargs)
        logger.info("cmd: %r ; opts %r", cmd, opts)
        return cmd, opts

    def update(self):

        self.data["freeRamMb"] = random.randint(0, self.data["totalRamMb"])
        self.data["freeSwapMb"] = random.randint(0, self.data["totalSwapMb"])

        phys = random.randint(0, self.data["physicalCpus"])
        self.data["load"] = tuple(phys * random.random() for _ in xrange(3))


    def sendPing(self, tasks, isReboot=False):
        if conf.NETWORK_DISABLED:
            return

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
        hw.load = self.load

        # Create a ping
        ping = ttypes.Ping()
        ping.hostname = self.__hostname
        ping.ipAddr = socket.gethostbyname(socket.getfqdn())
        ping.isReboot = isReboot
        ping.bootTime = self.bootTime
        ping.hw = hw
        ping.tasks = tasks

        logger.info("Sending ping: %s", ping)
        try:
            service, transport = client.getPlowConnection()
            service.sendPing(ping)
            transport.close()
        except Exception, e:
            logger.exception("Unable to send ping to plow server, %s", e)


