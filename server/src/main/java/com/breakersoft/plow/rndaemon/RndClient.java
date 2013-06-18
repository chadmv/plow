package com.breakersoft.plow.rndaemon;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.exceptions.RndClientExecuteException;
import com.breakersoft.plow.rnd.thrift.RndNodeApi;
import com.breakersoft.plow.rnd.thrift.RunTaskCommand;

public class RndClient {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(RndClient.class);

    private static final int MAX_RETRIES = 3;

    private final String host;
    private final int port;

    private TSocket socket = null;
    private TTransport transport = null;
    private TProtocol protocol = null;
    private RndNodeApi.Client service = null;

    public RndClient(String host) {
        this.host = host;
        this.port = 11338;
    }

    public String getHostname() {
        return this.host;
    }

    public void connect() throws TTransportException {

        if (socket!= null) {
            socket.close();
        }

        socket = new TSocket(host, port);
        socket.setTimeout(Defaults.RND_CLIENT_SOCKET_TIMEOUT_MS);
        transport = new TFramedTransport(socket);
        protocol = new TBinaryProtocol(transport);
        transport.open();
        service = new RndNodeApi.Client(protocol);
    }

    public synchronized void runProcess(RunTaskCommand command) {
        int retries = 0;
        while (true) {

            try {
                service.runTask(command);
                return;
            } catch (TTransportException e) {
                if (retries>=MAX_RETRIES) {
                    throw new RndClientExecuteException(e);
                }
                try {
                    connect();
                } catch (TTransportException te) {
                    logger.warn("Failed to reconnect to " + getHostname());
                }
            } catch (TException e) {
                logger.warn("Failed to run task " + command, e);
                throw new RndClientExecuteException(e);
            }

            retries++;
        }
    }

    public synchronized void kill(Proc proc, String reason) {
        int retries = 0;
        while (true) {

            try {
                service.killRunningTask(proc.getProcId().toString(), reason);
                return;
            } catch (TTransportException e) {
                if (retries>=MAX_RETRIES) {
                    throw new RndClientExecuteException(e);
                }
                try {
                    connect();
                } catch (TTransportException te) {
                    logger.warn("Failed to reconnect to " + getHostname());
                }
            } catch (TException e) {
                logger.warn("Failed to kill proc " + proc, e);
                throw new RndClientExecuteException(e);
            }

            retries++;
        }
    }
}
