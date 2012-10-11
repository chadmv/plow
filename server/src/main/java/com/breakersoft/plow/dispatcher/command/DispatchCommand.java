package com.breakersoft.plow.dispatcher.command;

import com.breakersoft.plow.dispatcher.BookingThread;

public interface DispatchCommand {

    public void execute(BookingThread thread);

}
