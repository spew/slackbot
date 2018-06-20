package org.poker.poller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PollerManager implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(PollerManager.class);

    private final List<Poller> pollers;
    private final List<ScheduledExecutorService> schedulers = new ArrayList<>();
    private final List<ScheduledFuture> futures = new ArrayList<>();

    public PollerManager(List<Poller> pollers) {
        this.pollers = pollers;
        startPollers();
    }

    @Override
    public void close() {
        stopPollers();
    }

    private void startPollers() {
        for (int i = 0; i < pollers.size(); i++) {
            schedulers.add(Executors.newScheduledThreadPool(1));
        }
        for (int i = 0; i < pollers.size(); i++) {
            Poller poller = pollers.get(i);
            schedulers.get(i).scheduleWithFixedDelay(newPollRunnable(poller), 0, poller.getIntervalValue(), poller.getIntervalTimeUnit());
        }
    }

    private Runnable newPollRunnable(Poller poller) {
        return () -> {
            try {
                poller.Poll();
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    throw e;
                }
                logger.error("error caught in poller", e);
            }
        };
    }

    private void stopPollers() {
        for (ScheduledExecutorService scheduler : schedulers) {
            scheduler.shutdownNow();
        }
        for (ScheduledExecutorService scheduler : schedulers) {
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        futures.clear();
        schedulers.clear();
    }
}
