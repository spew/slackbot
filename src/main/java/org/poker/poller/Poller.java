package org.poker.poller;

import java.util.concurrent.TimeUnit;

public interface Poller {
    void Poll();
    TimeUnit getIntervalTimeUnit();
    long getIntervalValue();
}
