package com.breakersoft.plow.dispatcher.pipeline;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.JobId;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Component
public class PipelineControllerImpl implements PipelineController {

    @Autowired
    @Qualifier("pipelineExecutor")
    private ThreadPoolTaskExecutor pipelineExecutor;

    private LoadingCache<UUID, Pipeline> pipelines;

    public PipelineControllerImpl() {
        pipelines = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .concurrencyLevel(16)
            .build(
                new CacheLoader<UUID, Pipeline>() {
                  public Pipeline load(UUID key)  {
                      return new Pipeline(key);
                  }
            });
    }

    public void execute(PipelineCommand command) {

        try {
            Pipeline pipeline = pipelines.get(command.getJobId());
            pipeline.add(command);
            pipelineExecutor.execute(pipeline);
        } catch (ExecutionException e) {
            System.err.println("Unable to add execution pipeline: " + e);
        }
    }

    public void execute(JobId id, Collection<PipelineCommand> commands) {
        try {
            Pipeline pipeline = pipelines.get(id.getJobId());
            pipeline.addAll(commands);
            pipelineExecutor.execute(pipeline);
        } catch (ExecutionException e) {
            System.err.println("Unable to add execution pipeline: " + e);
        }
    }

    public void waitForShutdown() throws InterruptedException {
        pipelineExecutor.getThreadPoolExecutor().shutdown();
        pipelineExecutor.getThreadPoolExecutor().awaitTermination(10, TimeUnit.SECONDS);
    }
}
