package com.breakersoft.plow.dispatcher.command;

import org.slf4j.Logger;

import com.breakersoft.plow.dispatcher.NodeDispatcher;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchResult;

public class BookNodeCommand implements Runnable {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(BookNodeCommand.class);

    private final DispatchNode node;
    private final NodeDispatcher dispatcher;

    public BookNodeCommand(DispatchNode node, NodeDispatcher dispatcher) {
        this.node = node;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        final DispatchResult result = new DispatchResult(node);
        dispatcher.dispatch(result, node);

        logger.info("BookNodeCommand dispatched: {} procs - {}/{}MB from {}",
                new Object[] { result.cores, result.procs.size(),
                result.ram, node.getName()});
    }
}
