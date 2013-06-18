package com.breakersoft.plow.crond;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.breakersoft.plow.dao.CrondDao;

/**
 * An abstract class that wraps the crond tasks in a try/finally.
 *
 * @author chambers
 *
 */
public abstract class AbstractCrondTask {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractCrondTask.class);

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
        catch (Exception e) {
            logger.error("Failed to execute crond task {}", taskType);
            logger.error("Unexpected exception, ", e);
        }
        finally {
            crondDao.unlock(taskType);
        }
    }

    protected abstract void run();

}
