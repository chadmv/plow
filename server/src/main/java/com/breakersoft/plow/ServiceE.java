package com.breakersoft.plow;

import java.util.UUID;

public class ServiceE implements Service {

    private UUID serviceId;
    private String name;

    public UUID getServiceId() {
        return serviceId;
    }
    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        Service other = (Service) obj;
        return serviceId.equals(other.getServiceId());
    }
}
