package com.breakersoft.plow.service;

import java.util.List;

import com.breakersoft.plow.Depend;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.thrift.DependSpecT;

public interface DependService {

    public Depend createDepend(DependSpecT spec);

    List<Depend> getOnJobDepends(Job job);

    List<Depend> getOnLayerDepends(Layer layer);

    List<Depend> getOnTaskDepends(Task task);

    boolean satisfyDepend(Depend depend);
}
