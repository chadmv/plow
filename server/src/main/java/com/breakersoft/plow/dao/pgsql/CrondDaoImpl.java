package com.breakersoft.plow.dao.pgsql;

import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.crond.CrondTask;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.CrondDao;

/**
 * Allows different nodes in the plow cluster to run the crond processes
 * without stomping on each other.
 *
 * ** Cronds are setup in the sprint root-context.xml file. **
 *
 * @author chambers
 */
@Repository
public class CrondDaoImpl extends AbstractDao implements CrondDao {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(CrondDaoImpl.class);

    private static final String LOCK =
        "UPDATE " +
            "plow.crond " +
        "SET " +
            "b_locked = 't', " +
            "time_started = plow.txTimeMillis(),"+
            "time_stopped = 0 ";

    private static final String LOCK_TYPE1 =
        LOCK +
        "WHERE " +
            "str_name = ? " +
        "AND " +
            "b_locked = 'f'";

    private static final String LOCK_TYPE2 =
        LOCK +
        "WHERE " +
            "str_name = ? " +
        "AND " +
            "b_locked = 't' " +
        "AND " +
            "plow.currentTimeMillis() - time_started  >= duration_timeout " +
        "AND " +
            "time_stopped = 0 ";

    public boolean lock(CrondTask task) {
        boolean result = false;
        result = jdbc.update(LOCK_TYPE1, task.toString()) == 1;
        if (!result) {
            result = jdbc.update(LOCK_TYPE2, task.toString()) == 1;
            if (result) {
                logger.warn("Obtained post timeout-lock on task: {}", task);
            }
        }
        if (!result) {
            logger.warn("Crond task {} failed to lock.", task);
        }
        return result;
    }

    private static final String UNLOCK =
            "UPDATE " +
                "plow.crond " +
            "SET " +
                "b_locked = 'f' " +
            "WHERE " +
                "str_name = ? " +
            "AND " +
                "b_locked = 't' ";

    public boolean unlock(CrondTask task) {
       boolean result = jdbc.update(UNLOCK, task.toString()) == 1;
       if (!result) {
           logger.warn("Crond task {} failed to unlock.", task);
       }
       return result;
    }
}
