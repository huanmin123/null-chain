package com.gitee.huanminabc.nullchain.utils;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public final class ThreadFactoryBuilder {
    private String nameFormat = null;
    public ThreadFactoryBuilder() {}
    public ThreadFactoryBuilder setNameFormat(String nameFormat) {
        this.nameFormat = nameFormat;
        return this;
    }

    public ThreadFactory build() {
        return doBuild(this);
    }

    private static ThreadFactory doBuild(ThreadFactoryBuilder builder) {
        final String nameFormat = builder.nameFormat;
        final ThreadFactory backingThreadFactory =
                Executors.defaultThreadFactory();
        final AtomicLong count = (nameFormat != null) ? new AtomicLong(0) : null;
        return runnable -> {
            Thread thread = backingThreadFactory.newThread(runnable);
            if (nameFormat != null) {
                thread.setName(format(nameFormat, count.getAndIncrement()));
            }
            return thread;
        };
    }

    private static String format(String format, Object... args) {
        return String.format(Locale.ROOT, format, args);
    }
}
