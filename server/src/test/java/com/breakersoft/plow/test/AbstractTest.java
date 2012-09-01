package com.breakersoft.plow.test;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.ProjectDao;
import com.breakersoft.plow.json.Blueprint;
import com.breakersoft.plow.json.BlueprintLayer;

@Transactional
@ContextConfiguration(locations={
        "file:src/main/webapp/WEB-INF/spring/root-context.xml"
    })
public abstract class AbstractTest extends AbstractTransactionalJUnit4SpringContextTests {


    @Resource
    ProjectDao projectDao;

    protected Project testProject;

    @Before
    public void initTestProject() {
        testProject = projectDao.create("test", "Test");
    }
    public Blueprint getTestBlueprint() {

        Blueprint bp = new Blueprint();
        bp.setName("test");
        bp.setPaused(false);
        bp.setProject("test");
        bp.setScene("seq");
        bp.setShot("shot");
        bp.setUid(100);
        bp.setUsername("gandalf");

        BlueprintLayer layer = new BlueprintLayer();
        layer.setChunk(1);
        layer.setCommand(new String[] { "/bin/ls" });
        layer.setMaxCores(8);
        layer.setMinCores(1);
        layer.setMinMemory(1024);
        layer.setName("test_ls");
        layer.setRange("1-10");

        bp.addLayer(layer);

        return bp;
    }

    public void assertFrameCount(Job job, int count) {
        assertEquals(count, simpleJdbcTemplate.queryForInt(
                "SELECT COUNT(1) FROM plow.frame, plow.layer " +
                "WHERE frame.pk_layer = layer.pk_layer AND layer.pk_job=?", job.getJobId()));
    }

    public void assertLayerCount(Job job, int count) {
        assertEquals(count, simpleJdbcTemplate.queryForInt(
                "SELECT COUNT(1) FROM plow.layer " +
                "WHERE layer.pk_job=?", job.getJobId()));
    }
}
