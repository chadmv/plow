package com.breakersoft.plow.exceptions;

public class ResourceAllocationException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ResourceAllocationException() {
        // TODO Auto-generated constructor stub
    }

    public ResourceAllocationException(String msg) {
        super(msg);
    }

    public ResourceAllocationException(Exception e) {
        super(e);
    }
}
