package com.breakersoft.plow;

import java.util.UUID;

public class ActionE implements Action {

	private UUID filterId;
	private UUID actionId;

	public void setFilterId(UUID filterId) {
		this.filterId = filterId;
	}

	public UUID getActionId() {
		return actionId;
	}

	public void setActionId(UUID actionId) {
		this.actionId = actionId;
	}

	@Override
	public UUID getFilterId() {
		return filterId;
	}

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        Action other = (Action) obj;
        return actionId.equals(other.getActionId());
    }
}
