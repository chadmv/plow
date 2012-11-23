package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.FolderT;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.dao.ThriftJobBoardDao;

public class ThriftJobBoardTests extends AbstractTest {

    @Resource
    ThriftJobBoardDao thriftJobBoard;

    @Test
    public void testGetJobBoard() {
        JobSpecT spec = getTestJobSpec();
        jobService.launch(spec);

        List<FolderT> result = thriftJobBoard.getJobBoard(
                TEST_PROJECT.getProjectId());

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).jobs.size());
    }
}
