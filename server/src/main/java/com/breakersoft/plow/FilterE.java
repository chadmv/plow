package com.breakersoft.plow;

import java.util.UUID;

public class FilterE implements Filter {

	private UUID filterId;

	public UUID getFilterId() {
		return filterId;
	}

	public void setFilterId(UUID filterId) {
		this.filterId = filterId;
	}

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        Filter other = (Filter) obj;
        return filterId.equals(other.getFilterId());
    }
}
