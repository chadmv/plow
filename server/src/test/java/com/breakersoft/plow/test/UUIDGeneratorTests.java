package com.breakersoft.plow.test;

import java.util.UUID;

import org.junit.Test;

import com.breakersoft.plow.util.UUIDGen;

public class UUIDGeneratorTests {

    @Test
    public void testGenerate10k() {

        System.out.println("RANDOM METHOD");


        long startTime = System.currentTimeMillis();
        for (int i=0; i<10000; i++) {
            UUID.randomUUID();
        }
        System.out.println(String.format("Random UUID: %d", System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        for (int i=0; i<10000; i++) {
            UUID.randomUUID();
        }
        System.out.println(String.format("Random UUID: %d", System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        for (int i=0; i<10000; i++) {
            UUID.randomUUID();
        }
        System.out.println(String.format("Random UUID: %d", System.currentTimeMillis() - startTime));

        System.out.println("UUID METHOD");

        for (int i=0; i<10000; i++) {
            UUIDGen.random();
        }
        System.out.println(String.format("Random UUID: %d", System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        for (int i=0; i<10000; i++) {
            UUIDGen.random();
        }
        System.out.println(String.format("Random UUID: %d", System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        for (int i=0; i<10000; i++) {
            UUIDGen.random();
        }
        System.out.println(String.format("Random UUID: %d", System.currentTimeMillis() - startTime));


    }

}
