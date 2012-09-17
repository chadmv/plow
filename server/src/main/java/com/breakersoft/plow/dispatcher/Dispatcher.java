package com.breakersoft.plow.dispatcher;

public interface Dispatcher {

    DispatchNode getNextDispatchNode();

    void dispatch(DispatchJob job, DispatchNode node);

}
