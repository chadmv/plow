package com.breakersoft.plow.thrift.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.thrift.DependT;

public interface ThriftDependDao {

    List<DependT> getWhatDependsOnJob(UUID jobId);
    List<DependT> getWhatDependsOnLayer(UUID layerId);
    List<DependT> getWhatDependsOnTask(UUID taskId);
    List<DependT> getWhatJobDependsOn(UUID jobId);
    List<DependT> getWhatLayerDependsOn(UUID layerId);
    List<DependT> getWhatTaskDependsOn(UUID taskId);
    DependT getDepend(UUID dependId);
}
