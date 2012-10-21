package com.breakersoft.plow.thrift.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.thrift.LayerT;


public interface ThriftLayerDao {

    LayerT getLayer(UUID id);

    List<LayerT> getLayers(UUID jobId);

}
