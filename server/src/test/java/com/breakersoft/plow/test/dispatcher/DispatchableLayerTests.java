package com.breakersoft.plow.test.dispatcher;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.dispatcher.DispatchFolder;
import com.breakersoft.plow.dispatcher.DispatchJob;
import com.breakersoft.plow.dispatcher.DispatchLayer;
import com.breakersoft.plow.dispatcher.Dispatcher;
import com.breakersoft.plow.service.DispatcherService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class DispatchableLayerTests {

    @Autowired
    DispatcherService dispatcherService;

    @Autowired
    Dispatcher dispatcher;

    @Test
    public void sortTest() {


    }

}
