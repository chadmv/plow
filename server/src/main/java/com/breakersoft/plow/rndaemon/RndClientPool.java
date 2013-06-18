package com.breakersoft.plow.rndaemon;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.domain.DispatchPair;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchResult;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.exceptions.RndClientConnectionError;
import com.breakersoft.plow.rnd.thrift.RunTaskCommand;
import com.breakersoft.plow.util.PlowUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Component
public class RndClientPool {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(RndClientPool.class);

    // Cache that maps a path to a group
    private final LoadingCache<String, RndClient> rndClientCache;

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    @Qualifier("rndCommandExecutor")
    private ThreadPoolTaskExecutor rndCommandExecutor;

    public RndClientPool() {
        rndClientCache = CacheBuilder.newBuilder()
            .initialCapacity(200)
            .concurrencyLevel(8)
            .build(new CacheLoader<String, RndClient>() {
                @Override
                public RndClient load(final String key) throws Exception {
                    RndClient client = new RndClient(key);
                    client.connect();
                    return client;
                }
            });
    }

    public RndClient get(String hostname) {
        try {
            return rndClientCache.get(hostname);
        } catch (ExecutionException e) {
            throw new RndClientConnectionError("Rnd connection error: " + e, e);
        }
    }

    public void executeProcess(DispatchProc proc, DispatchTask task) {
        rndCommandExecutor.execute(new RunProcessCommand(proc, task));
    }

    private class RunProcessCommand implements Runnable {

        private final DispatchProc proc;
        private final DispatchTask task;

        public RunProcessCommand(DispatchProc proc, DispatchTask task) {
            this.proc = proc;
            this.task = task;
        }

        @Override
        public void run() {

            final RunTaskCommand command =
                    dispatchService.getRuntaskCommand(task);

            try {
                RndClient client = rndClientCache.get(proc.getHostname());
                client.runProcess(command);
                return;
            }
            catch (ExecutionException e) {
                logger.error("Unable to obtain cached RND client connection: " + e);
            }
            catch (RuntimeException e) {
                logger.error("Failed to execute run process on host: " + proc.getHostname() + "," + e);
            }

            // Already returned on success, handling failure by marking the proc
            // as an orphan.
            dispatchService.setProcDeallocated(proc);
        }
    }
}
