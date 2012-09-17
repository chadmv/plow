package com.breakersoft.plow.rpc;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;

public class RpcServer {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(RpcServer.class);

    private final int port;
    private final String name;
    private final TProcessor processor;
    private final Thread thread;

    private TNonblockingServerTransport transport;
    private TServer server;

    public RpcServer(TProcessor processor, int port) {
        this.port = port;
        this.name = processor.toString();
        this.processor = processor;
        thread = new ServerThread();
    }

    @PostConstruct
    public void start() {
        logger.info("Starting thift server " + name + " on port " + port);

        try {
            transport = new TNonblockingServerSocket(port);
            server = new TThreadedSelectorServer(
                    new TThreadedSelectorServer.Args(transport)
                .processor(processor)
                .workerThreads(16)
                .selectorThreads(4));
            thread.start();

        } catch (TTransportException e) {
             e.printStackTrace();
        }
    }

    @PreDestroy
    public void stop() {
        logger.info("Stopping thift server " + name + " on port " + port);
        server.stop();
    }

    private class ServerThread extends Thread {

        public void run() {
            server.serve();
        }
    }
}
