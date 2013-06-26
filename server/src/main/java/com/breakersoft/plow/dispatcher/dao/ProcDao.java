package com.breakersoft.plow.dispatcher.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.JobId;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;

public interface ProcDao {

    Proc getProc(Task frame);

    Proc getProc(UUID procId);

    boolean delete(Proc proc);

    List<Proc> getProcs(JobId job);

    boolean setProcUnbooked(Proc proc, boolean unbooked);

    public void create(DispatchProc proc);

    boolean unassign(Proc proc);

    boolean assign(Proc proc, Task task);

    /**
     * Marks the proc as deallocated, the sweeper will remove it
     * and possibly redispatch the node if its scheduled.
     *
     * @param proc
     * @return
     */
    boolean setProcDeallocated(Proc proc);

    boolean unassignAndMarkForDeallocation(Proc proc);
}
