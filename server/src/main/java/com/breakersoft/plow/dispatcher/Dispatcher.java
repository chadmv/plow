package com.breakersoft.plow.dispatcher;

import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchResult;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;

public interface Dispatcher<T> {

    DispatchResult dispatch(T resource);

    void dispatch(DispatchResult result, T resource);

    void dispatch(DispatchResult result, T resource, DispatchProject project);

    void dispatch(DispatchResult result, T resource, DispatchJob job);

    public void dispatch(DispatchResult result, T resource, DispatchTask task);
}
