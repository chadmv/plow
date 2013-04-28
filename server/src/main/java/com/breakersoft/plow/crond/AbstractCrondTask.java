package com.breakersoft.plow.crond;

import javax.annotation.Resource;

import com.breakersoft.plow.dao.CrondDao;

/**
 * An abstract class that wraps the crond tasks in a try/finally.
 *
 * @author chambers
 *
 */
public abstract class AbstractCrondTask {

    @Resource
    CrondDao crondDao;

    private final CrondTask taskType;

    public AbstractCrondTask(CrondTask taskType) {
        this.taskType = taskType;
    }

    public void start() {
        if (!crondDao.lock(taskType)) {
            return;
        }
        try {
            run();
        }
        finally {
            crondDao.unlock(taskType);
        }
    }

    protected abstract void run();

}
