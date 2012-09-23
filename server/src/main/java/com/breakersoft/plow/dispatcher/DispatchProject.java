package com.breakersoft.plow.dispatcher;

import java.util.UUID;

import com.breakersoft.plow.ProjectE;

public class DispatchProject extends ProjectE {

    private float tier;
    private UUID quotaId;

    public DispatchProject() { }

    public void setTier(float tier) {
        this.tier = tier;
    }

    public float getTier() {
        return this.tier;
    }

    public UUID getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(UUID quotaId) {
        this.quotaId = quotaId;
    }
}
