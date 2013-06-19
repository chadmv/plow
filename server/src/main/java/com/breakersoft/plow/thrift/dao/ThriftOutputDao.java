package com.breakersoft.plow.thrift.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.thrift.OutputT;

public interface ThriftOutputDao {

    List<OutputT> getOutputs(Layer layerId);

    List<OutputT> getOutputs(Job jobId);

    List<OutputT> getLayerOutputs(UUID layerId);

    List<OutputT> getJobOutputs(UUID jobId);

    OutputT getOutput(UUID id);
}
