package com.breakersoft.plow.test.service;

import static org.junit.Assert.*;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.ActionFull;
import com.breakersoft.plow.Filter;
import com.breakersoft.plow.Folder;
import com.breakersoft.plow.MatcherFull;
import com.breakersoft.plow.dao.ActionDao;
import com.breakersoft.plow.dao.FolderDao;
import com.breakersoft.plow.dao.MatcherDao;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.FilterService;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.ActionType;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.MatcherField;
import com.breakersoft.plow.thrift.MatcherType;

public class FilterServiceTests extends AbstractTest {

    @Resource
    FilterService filterService;

    @Resource
    MatcherDao matcherDao;

    @Resource
    ActionDao actionDao;

    @Resource
    FolderDao folderDao;

    @Test
    public void testMatchJob() {

        Filter f1 = filterService.createFilter(TEST_PROJECT, "test");

        MatcherFull contains =  matcherDao.getFull(filterService.createMatcher(
                f1, MatcherField.JOB_NAME, MatcherType.CONTAINS, "test"));

        MatcherFull not_contains = matcherDao.getFull(filterService.createMatcher(
                f1, MatcherField.JOB_NAME, MatcherType.NOT_CONTAINS, "horse"));

        MatcherFull is = matcherDao.getFull(filterService.createMatcher(
                f1, MatcherField.JOB_NAME, MatcherType.IS, "depend_test_v1"));

        MatcherFull is_not = matcherDao.getFull(filterService.createMatcher(
                f1, MatcherField.JOB_NAME, MatcherType.IS_NOT, "horse"));

        MatcherFull begins_with = matcherDao.getFull(filterService.createMatcher(
                f1, MatcherField.JOB_NAME, MatcherType.BEGINS_WITH, "depend"));

        MatcherFull ends_with = matcherDao.getFull(filterService.createMatcher(
                f1, MatcherField.JOB_NAME, MatcherType.ENDS_WITH, "1"));

        JobSpecT spec1 = getTestJobSpec("depend_test_v1");
        JobLaunchEvent event = jobService.launch(spec1);

        assertTrue(filterService.matchJob(contains, event.getJob()));
        assertTrue(filterService.matchJob(not_contains, event.getJob()));
        assertTrue(filterService.matchJob(is, event.getJob()));
        assertTrue(filterService.matchJob(is_not, event.getJob()));
        assertTrue(filterService.matchJob(begins_with, event.getJob()));
        assertTrue(filterService.matchJob(ends_with, event.getJob()));
    }

    @Test
    public void testApplySetMinCoresAction() {

        JobSpecT spec1 = getTestJobSpec("depend_test_v1");
        JobLaunchEvent event = jobService.launch(spec1);

        Filter f1 = filterService.createFilter(TEST_PROJECT, "test");
        ActionFull action = actionDao.getFull(
                filterService.createAction(f1, ActionType.SET_MIN_CORES, "10"));
        filterService.applyAction(action, event.getJob());

        int value = jdbc().queryForInt(
                "SELECT int_cores_min FROM plow.job_dsp WHERE pk_job=?",  event.getJob().getJobId());
        assertEquals(10, value);
    }

    @Test
    public void testApplySetMaxCoresAction() {

        JobSpecT spec1 = getTestJobSpec("depend_test_v1");
        JobLaunchEvent event = jobService.launch(spec1);

        Filter f1 = filterService.createFilter(TEST_PROJECT, "test");
        ActionFull action = actionDao.getFull(
                filterService.createAction(f1, ActionType.SET_MAX_CORES, "10"));
        filterService.applyAction(action, event.getJob());

        int value = jdbc().queryForInt(
                "SELECT int_cores_max FROM plow.job_dsp WHERE pk_job=?",  event.getJob().getJobId());
        assertEquals(10, value);
    }

    @Test
    public void testApplyPauseAction() {

        JobSpecT spec1 = getTestJobSpec("depend_test_v1");
        JobLaunchEvent event = jobService.launch(spec1);

        Filter f1 = filterService.createFilter(TEST_PROJECT, "test");
        ActionFull action = actionDao.getFull(
                filterService.createAction(f1, ActionType.PAUSE, "true"));
        filterService.applyAction(action, event.getJob());

        int value = jdbc().queryForInt(
                "SELECT bool_paused::integer FROM plow.job  WHERE pk_job=?",  event.getJob().getJobId());
        assertEquals(1, value);

        action = actionDao.getFull(
                filterService.createAction(f1, ActionType.PAUSE, "false"));
        filterService.applyAction(action, event.getJob());
        value = jdbc().queryForInt(
                "SELECT bool_paused::integer FROM plow.job  WHERE pk_job=?",  event.getJob().getJobId());
        assertEquals(0, value);
    }

    @Test
    public void testApplyFolderAction() {

        JobSpecT spec1 = getTestJobSpec("depend_test_v1");
        JobLaunchEvent event = jobService.launch(spec1);

        Folder folder = folderDao.createFolder(TEST_PROJECT, "test_folder");

        Filter f1 = filterService.createFilter(TEST_PROJECT, "test");
        ActionFull action = actionDao.getFull(
                filterService.createAction(f1, ActionType.SET_FOLDER, folder.getFolderId().toString()));

        filterService.applyAction(action, event.getJob());

        UUID folderId = (UUID) jdbc().queryForObject(
                "SELECT pk_folder FROM plow.job  WHERE pk_job=?", UUID.class, event.getJob().getJobId());
        assertEquals(folder.getFolderId(), folderId);
    }
}
