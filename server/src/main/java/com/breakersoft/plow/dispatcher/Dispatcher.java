package com.breakersoft.plow.dispatcher;

import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchResult;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;

public interface Dispatcher<T> {

    void dispatch(DispatchResult result, T resource);
    void dispatch(DispatchResult result, T resource, DispatchProject project);
    void dispatch(DispatchResult result, T resource, DispatchJob job);
    void dispatch(DispatchResult result, T resource, DispatchTask task);

    void dispatchFailed(DispatchResult result, DispatchProc proc, DispatchTask task, String message);

}
