package com.breakersoft.plow.rnd;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.server.THsHaServer;
import org.slf4j.Logger;

public class RndServer {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(RndServer.class);

    private final int port;
    private final TProcessor processor;
    private final Thread thread;

    private TNonblockingServerTransport transport;
    private TServer server;

    public RndServer(TProcessor processor, int port) {
        this.port = port;
        this.processor = processor;
        thread = new ServerThread();
    }

    @PostConstruct
    public void start() {
        logger.info("Starting RND thrift server on port " + port);

        try {
            transport = new TNonblockingServerSocket(port);
            server = new THsHaServer(new THsHaServer.Args(transport)
                .processor(processor)
                .workerThreads(8));
            thread.start();

        } catch (TTransportException e) {
             e.printStackTrace();
        }
    }

    @PreDestroy
    public void stop() {
        logger.info("Stopping RND thift server on port " + port);
        server.stop();
    }

    private class ServerThread extends Thread {
        public void run() {
            server.serve();
        }
    }
}
