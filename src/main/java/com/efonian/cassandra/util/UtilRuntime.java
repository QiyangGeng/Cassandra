package com.efonian.cassandra.util;

import com.efonian.cassandra.misc.DaemonThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.concurrent.*;


@Component
@Scope(value= ConfigurableBeanFactory.SCOPE_SINGLETON)
public final class UtilRuntime {
    private static ApplicationContext ctx;
    private static ApplicationArguments args;
    private static Instant startTime;
    
    // This is meant to be an executor available to any class in the program so that they do not need to create their
    // own executor service if they don't want to.
    // Not expecting frequent usage, tasks queued when the service has reached the maximum pool size will be rejected,
    // so do not abuse and limit access.
    // The above behavior can be changed by changing the SynchronousQueue to some other queue, most likely
    // LinkedBlockingQueue. Other parameters can also be played around.
    private static final ExecutorService auxiliaryExecutorService =
            new ThreadPoolExecutor(0, 16,
                    30, TimeUnit.SECONDS, new SynchronousQueue<>(), new DaemonThreadFactory());
    
    public static void shutdown() {
        shutdown(0);
    }
    
    public static void shutdown(int code) {
        shutDownExecutorService(auxiliaryExecutorService);
        System.exit(
                SpringApplication.exit(ctx, () -> code));
    }
    
    @PostConstruct
    private void init() {
        startTime = Instant.now();
    }
    
    @Autowired
    private void setApplicationArguments(ApplicationArguments args) {
        UtilRuntime.args = args;
    }
    
    @Autowired
    private void setApplicationContext(ApplicationContext ctx) {
        UtilRuntime.ctx = ctx;
    }
    
    public ApplicationArguments getArgs() {
        return args;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public static void executeTask(Runnable task) {
        auxiliaryExecutorService.execute(task);
    }
    
    public static Future<?> submitTask(Runnable task) {
        return auxiliaryExecutorService.submit(task);
    }
    
    public static Future<?> submitTask(Callable<?> task) {
        return auxiliaryExecutorService.submit(task);
    }
    
    public static <T> Future<T> submitTask(Runnable task, T result) {
        return auxiliaryExecutorService.submit(task, result);
    }
    
    /**
     * Shuts down an executor service with a 1 second timeout
     * @param service the executor service to be shut down
     */
    public static void shutDownExecutorService(ExecutorService service) {
        shutDownExecutorService(service, 1, TimeUnit.SECONDS);
    }
    
    /**
     * Shuts down an executor service with a given timeout
     * @param service the executor service to be shut down
     * @param timeout the length of the timeout
     * @param unit unit for the timeout
     */
    public static void shutDownExecutorService(ExecutorService service, long timeout, TimeUnit unit) {
        service.shutdown();
        try {
            if (!service.awaitTermination(timeout, unit))
                service.shutdownNow();
        } catch (InterruptedException e) {
            service.shutdownNow();
        }
    }
}
