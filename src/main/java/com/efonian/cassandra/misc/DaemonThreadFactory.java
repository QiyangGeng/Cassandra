package com.efonian.cassandra.misc;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

public class DaemonThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    }
}
