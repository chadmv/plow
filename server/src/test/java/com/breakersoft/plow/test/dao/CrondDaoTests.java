package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.crond.CrondTask;
import com.breakersoft.plow.dao.CrondDao;
import com.breakersoft.plow.test.AbstractTest;

public class CrondDaoTests extends AbstractTest {

    @Resource
    CrondDao crondDao;

    @Test
    public void testLockType1() {
        assertTrue(crondDao.lock(CrondTask.ORPHAN_PROC_CHECK));
        assertFalse(crondDao.lock(CrondTask.ORPHAN_PROC_CHECK));
    }

    @Test
    public void testLockType2() throws InterruptedException {
        assertTrue(crondDao.lock(CrondTask.ORPHAN_PROC_CHECK));

        // change the timeout duration to 1/4 a second
        jdbc().update("UPDATE plow.crond SET duration_timeout=250 WHERE str_name=?",
                CrondTask.ORPHAN_PROC_CHECK.toString());

        // Now sleep so the task timesout
        Thread.sleep(251);

        assertTrue(crondDao.lock(CrondTask.ORPHAN_PROC_CHECK));
    }

    @Test
    public void testUnlockType1() {
        assertTrue(crondDao.lock(CrondTask.ORPHAN_PROC_CHECK));
        assertFalse(crondDao.lock(CrondTask.ORPHAN_PROC_CHECK));
        assertTrue(crondDao.unlock(CrondTask.ORPHAN_PROC_CHECK));
        assertFalse(crondDao.unlock(CrondTask.ORPHAN_PROC_CHECK));
    }

    @Test
    public void testUnlockType2() throws InterruptedException {
        assertTrue(crondDao.lock(CrondTask.ORPHAN_PROC_CHECK));

        // change the timeout duration to 1/4 a second
        jdbc().update("UPDATE plow.crond SET duration_timeout=250 WHERE str_name=?",
                CrondTask.ORPHAN_PROC_CHECK.toString());

        // Now sleep so the task timesout
        Thread.sleep(251);
        assertTrue(crondDao.lock(CrondTask.ORPHAN_PROC_CHECK));
        assertTrue(crondDao.unlock(CrondTask.ORPHAN_PROC_CHECK));
        assertFalse(crondDao.unlock(CrondTask.ORPHAN_PROC_CHECK));
        assertTrue(crondDao.lock(CrondTask.ORPHAN_PROC_CHECK));
    }
}
