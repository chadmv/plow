package com.breakersoft.plow.dispatcher.command;

import com.breakersoft.plow.dispatcher.ProcDispatcher;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchResult;

public class BookProcCommand implements Runnable {

    final DispatchProc proc;
    final ProcDispatcher dispatcher;

    public BookProcCommand(DispatchProc proc, ProcDispatcher dispatcher) {
        this.proc = proc;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
         final DispatchResult result = new DispatchResult(proc);
         dispatcher.dispatch(result, proc);
    }
}
