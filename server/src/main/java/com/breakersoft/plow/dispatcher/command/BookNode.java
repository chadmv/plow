package com.breakersoft.plow.dispatcher.command;

import com.breakersoft.plow.dispatcher.BookingThread;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;

public class BookNode implements BookingCommand {

    private DispatchNode dispatchNode;

    public BookNode(DispatchNode node) {
        this.dispatchNode = node;
    }

    @Override
    public void execute(BookingThread thread) {
        thread.update();
        thread.book(dispatchNode);
    }
}
