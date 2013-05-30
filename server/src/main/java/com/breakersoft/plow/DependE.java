package com.breakersoft.plow;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.breakersoft.plow.thrift.DependType;

public class DependE implements Depend {

    private UUID dependId;
    private DependType type;
    private boolean active;

    private UUID dependentJobId;
    private UUID dependOnJobId;
    private UUID dependentLayerId;
    private UUID dependOnLayerId;
    private UUID dependentTaskId;
    private UUID dependOnTaskId;

    @Override
    public UUID getDependId() {
        return dependId;
    }

    @Override
    public DependType getType() {
        return type;
    }

    @Override
    public UUID getDependentJobId() {
        return dependentJobId;
    }

    @Override
    public UUID getDependOnJobId() {
        return dependOnJobId;
    }

    @Override
    public UUID getDependentLayerId() {
        return dependentLayerId;
    }

    @Override
    public UUID getDependOnLayerId() {
        return dependOnLayerId;
    }

    @Override
    public UUID getDependentTaskId() {
        return dependentTaskId;
    }

    @Override
    public UUID getDependOnTaskId() {
        return dependOnTaskId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setDependId(UUID dependId) {
        this.dependId = dependId;
    }

    public void setType(DependType type) {
        this.type = type;
    }

    public void setDependentJobId(UUID dependentJobId) {
        this.dependentJobId = dependentJobId;
    }

    public void setDependOnJobId(UUID dependOnJobId) {
        this.dependOnJobId = dependOnJobId;
    }

    public void setDependentLayerId(UUID dependentLayerId) {
        this.dependentLayerId = dependentLayerId;
    }

    public void setDependOnLayerId(UUID dependOnLayerId) {
        this.dependOnLayerId = dependOnLayerId;
    }

    public void setDependentTaskId(UUID dependentTaskId) {
        this.dependentTaskId = dependentTaskId;
    }

    public void setDependOnTaskId(UUID dependOnTaskId) {
        this.dependOnTaskId = dependOnTaskId;
    }

    public int hashCode() {
        return dependId.hashCode();
    }

    public UUID genSig() {
        final ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putInt(type.ordinal());

        switch (type) {
        case JOB_ON_JOB:
            buffer.putInt(dependentJobId.hashCode());
            buffer.putInt(dependOnJobId.hashCode());
            break;

        case LAYER_ON_LAYER:
            buffer.putInt(dependentLayerId.hashCode());
            buffer.putInt(dependOnLayerId.hashCode());
            break;

        case LAYER_ON_TASK:
            buffer.putInt(dependentLayerId.hashCode());
            buffer.putInt(dependOnTaskId.hashCode());
            break;

        case TASK_ON_LAYER:
            buffer.putInt(dependentTaskId.hashCode());
            buffer.putInt(dependOnLayerId.hashCode());
            break;

        case TASK_ON_TASK:
            buffer.putInt(dependentTaskId.hashCode());
            buffer.putInt(dependOnTaskId.hashCode());
            break;

        default:
            throw new RuntimeException("Unhandled depend type: " + type.toString());
        }

        return UUID.nameUUIDFromBytes(buffer.array());
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        Depend other = (Depend) obj;
        return dependId.equals(other.getDependId());
    }
}
