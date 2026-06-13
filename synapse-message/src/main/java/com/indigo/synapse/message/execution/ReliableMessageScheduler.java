package com.indigo.synapse.message.execution;

import com.indigo.synapse.message.autoconfigure.SynapseReliableMessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 可靠消息扫描器。
 */
public final class ReliableMessageScheduler implements SmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReliableMessageScheduler.class);

    private final ReliableMessageDispatcher dispatcher;
    private final SynapseReliableMessageProperties properties;
    private final String workerId;
    private volatile boolean running;
    private ScheduledExecutorService executorService;

    public ReliableMessageScheduler(ReliableMessageDispatcher dispatcher, SynapseReliableMessageProperties properties) {
        if (dispatcher == null || properties == null) {
            throw new IllegalArgumentException("scheduler dependencies must not be null");
        }
        this.dispatcher = dispatcher;
        this.properties = properties;
        this.workerId = "synapse-message-" + UUID.randomUUID();
    }

    public int runOnce() {
        return dispatcher.dispatchDue(
                workerId,
                properties.getScheduler().getBatchSize(),
                properties.getScheduler().getLockTtl()
        );
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, workerId + "-scanner");
            thread.setDaemon(true);
            return thread;
        });
        long intervalMillis = properties.getScheduler().getInterval().toMillis();
        executorService.scheduleWithFixedDelay(this::runOnceSafely, 0, intervalMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        running = false;
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void runOnceSafely() {
        if (!running) {
            return;
        }
        try {
            runOnce();
        } catch (RuntimeException ex) {
            // 后台调度不能因单次数据库或 MQ 异常永久退出，失败消息仍依赖租约超时和重试策略恢复。
            LOGGER.warn("可靠消息扫描执行失败，将在下一周期继续", ex);
        }
    }
}
