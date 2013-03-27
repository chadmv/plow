package com.breakersoft.plow;

import java.util.UUID;

public class MatcherE implements Matcher {

	private UUID filterId;
	private UUID matcherId;

	public void setFilterId(UUID filterId) {
		this.filterId = filterId;
	}

	public void setMatcherId(UUID matcherId) {
		this.matcherId = matcherId;
	}

	@Override
	public UUID getFilterId() {
		return filterId;
	}

	@Override
	public UUID getMatcherId() {
		return matcherId;
	}


    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        Matcher other = (Matcher) obj;
        return matcherId.equals(other.getMatcherId());
    }
}
