package com.breakersoft.plow;

import java.util.UUID;

public interface Quota extends Project, Cluster {

    UUID getQuotaId();

}
