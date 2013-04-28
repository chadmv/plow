package com.breakersoft.plow.dao;

import com.breakersoft.plow.crond.CrondTask;

public interface CrondDao {

    /**
     * Lock the given task type.  Return true if it locked,
     * false if it was already locked.
     *
     * @param task
     * @return
     */
    public boolean lock(CrondTask task);

    /**
     * Unlock the given task type.  Return true if it unlocked,
     * false if it did not.
     *
     * @param task
     * @return
     */
    public boolean unlock(CrondTask task);
}
