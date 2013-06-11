package com.breakersoft.plow.thrift.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.thrift.ProcFilterT;
import com.breakersoft.plow.thrift.ProcT;

public interface ThriftProcDao {

    List<ProcT> getProcs(ProcFilterT filter);

    ProcT getProc(UUID id);

}
