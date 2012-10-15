package com.breakersoft.plow.dispatcher.command;

import com.breakersoft.plow.dispatcher.BookingThread;

public interface BookingCommand {

    public void execute(BookingThread thread);

}
