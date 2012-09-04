package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.json.BlueprintLayer;

public interface LayerDao {

    Layer create(Job job, BlueprintLayer layer, int order);

    Layer get(Job job, String name);

    Layer get(UUID id);
}
