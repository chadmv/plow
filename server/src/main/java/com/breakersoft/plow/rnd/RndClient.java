package com.breakersoft.plow.rnd;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import com.breakersoft.plow.rnd.thrift.RndException;
import com.breakersoft.plow.rnd.thrift.RndNodeApi;
import com.breakersoft.plow.rnd.thrift.RunProcessCommand;

public class RndClient {

    private final String host;
    private final int port;

    private TSocket transport;
    private TProtocol protocol;
    private RndNodeApi.Client proxy;

    public RndClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.connect();
    }

    public void connect() {

        transport = new TSocket(host, port);
        protocol = new TBinaryProtocol(transport);

        try {
            transport.open();
        } catch (TTransportException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        proxy= new RndNodeApi.Client(protocol);
    }

    public void runProcess(RunProcessCommand command) {
        try {
            proxy.runProcess(command);
        } catch (RndException | TException e) {

        }
    }
}
