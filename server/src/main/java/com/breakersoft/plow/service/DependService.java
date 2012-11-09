package com.breakersoft.plow.service;

import com.breakersoft.plow.Depend;
import com.breakersoft.plow.thrift.DependSpecT;

public interface DependService {

    public Depend createDepend(DependSpecT spec);
}
