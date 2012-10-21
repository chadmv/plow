package com.breakersoft.plow.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.exceptions.RndClientExecuteException;
import com.breakersoft.plow.rnd.thrift.RndException;
import com.breakersoft.plow.rnd.thrift.RndNodeApi;
import com.breakersoft.plow.rnd.thrift.RunTaskCommand;

public class RndClient {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(RndClient.class);

    private final String host;
    private final int port;

    private TSocket socket;
    private TTransport transport;
    private TProtocol protocol;

    public RndClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /*
    TFramedTransport
    socket = TSocket.TSocket(host, int(port))
    transport = TTransport.TFramedTransport(socket)
    protocol = TBinaryProtocol.TBinaryProtocol(transport)
    service = RndServiceApi.Client(protocol)
    */
    public RndNodeApi.Client connect() throws TTransportException {
        socket = new TSocket(host, port);
        socket.setTimeout(Defaults.RND_CLIENT_SOCKET_TIMEOUT_MS);
        transport = new TFramedTransport(socket);
        protocol = new TBinaryProtocol(transport);
        transport.open();
        return new RndNodeApi.Client(protocol);
    }

    public void runProcess(RunTaskCommand command) {
        try {
            connect().runTask(command);
        } catch (TException e) {
            throw new RndClientExecuteException(e);
        }
        finally {
            socket.close();
        }
    }
}
