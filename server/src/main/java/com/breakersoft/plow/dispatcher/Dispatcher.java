package com.breakersoft.plow.dispatcher;

public interface Dispatcher {

    DispatchNode getNextDispatchNode();

    void dispatch(DispatchJob job, DispatchNode node);

    void dispatch(DispatchLayer layer, DispatchNode node);

    void dispatch(DispatchNode node);

    boolean canDispatch(DispatchLayer layer, DispatchNode node);
}
