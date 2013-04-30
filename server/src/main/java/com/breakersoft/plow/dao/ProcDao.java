package com.breakersoft.plow.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.JobId;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;

public interface ProcDao {

    Proc getProc(Task frame);

    Proc getProc(UUID procId);

    boolean delete(Proc proc);

    List<Proc> getProcs(JobId job);

    boolean setProcUnbooked(Proc proc, boolean unbooked);

    DispatchProc create(DispatchNode node, DispatchTask task);

    boolean unassign(Proc proc);

    boolean assign(Proc proc, Task task);
}
