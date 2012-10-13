package com.breakersoft.plow.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;

import com.breakersoft.plow.exceptions.RndClientExecuteException;
import com.breakersoft.plow.rnd.thrift.RndException;
import com.breakersoft.plow.rnd.thrift.RndNodeApi;
import com.breakersoft.plow.rnd.thrift.RunTaskCommand;

public class RndClient {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(RndClient.class);

    private final String host;
    private final int port;

    private TSocket transport;
    private TProtocol protocol;

    public RndClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RndNodeApi.Client connect() throws TTransportException {
        transport = new TSocket(host, port);
        protocol = new TBinaryProtocol(transport);
        transport.open();
        return new RndNodeApi.Client(protocol);
    }

    public void runProcess(RunTaskCommand command) {
        try {
            connect().runTask(command);
        } catch (RndException | TException e) {
            throw new RndClientExecuteException(e);
        }
        finally {
            transport.close();
        }
    }
}
