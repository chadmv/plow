package com.breakersoft.plow.dispatcher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class DispatchConfiguration {

    @Bean
    @Scope("prototype")
    public BookingThread getBookingThread() {
        return new BookingThread();
    }

    @Bean
    public FrontEndDispatcher getFrontEndDispatcher() {
        return new FrontEndDispatcher();
    }

    @Bean
    public BackEndDispatcher getBackEndDispatcher() throws Exception {
        return new BackEndDispatcher();
    }

    @Bean
    public DispatchSupport getDispatchSupport() {
        return new DispatchSupport();
    }
}
