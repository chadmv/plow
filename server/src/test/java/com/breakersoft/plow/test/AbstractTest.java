package com.breakersoft.plow.test;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.json.Blueprint;
import com.breakersoft.plow.json.BlueprintLayer;

@Transactional
@ContextConfiguration(locations={
        "file:src/main/webapp/WEB-INF/spring/root-context.xml"
    })
public abstract class AbstractTest extends AbstractTransactionalJUnit4SpringContextTests {
	
	
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

}
