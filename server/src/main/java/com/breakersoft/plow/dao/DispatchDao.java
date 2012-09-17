package com.breakersoft.plow.dao;

import java.util.List;

import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.DispatchTask;
import com.breakersoft.plow.dispatcher.DispatchJob;
import com.breakersoft.plow.dispatcher.DispatchNode;

public interface DispatchDao {

    boolean reserveFrame(Task frame);

    boolean unReserveFrame(Task frame);

    List<DispatchTask> getFrames(DispatchJob job, DispatchNode node);
}
