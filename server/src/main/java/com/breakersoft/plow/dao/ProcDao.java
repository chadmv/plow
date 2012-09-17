package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Task;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.dispatcher.DispatchProc;

public interface ProcDao {

    Proc getProc(Task frame);

    Proc getProc(UUID procId);

    void create(DispatchProc proc);

}
