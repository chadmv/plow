package com.breakersoft.plow.thrift.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.thrift.LayerT;
import com.breakersoft.plow.thrift.OutputT;


public interface ThriftLayerDao {

    LayerT getLayer(UUID id);

    List<LayerT> getLayers(UUID jobId);

    List<OutputT> getOutputs(UUID layerId);

    LayerT getLayer(UUID jobId, String name);

}
