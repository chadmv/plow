package com.breakersoft.plow.util;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.RandomBasedGenerator;

public final class UUIDGen {

    private static final EthernetAddress NIC = EthernetAddress.fromInterface();
    private static final RandomBasedGenerator TIME_BASED_GENERATOR = Generators.randomBasedGenerator();
    private static final ArrayBlockingQueue<UUID> CACHE = new ArrayBlockingQueue<UUID>(10000);

    public static final UUID random() {
        return TIME_BASED_GENERATOR.generate();
    }
}
