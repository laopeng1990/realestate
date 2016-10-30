package com.wpf.realestate.task;

import com.wpf.realestate.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by laopengwork on 2016/10/30.
 */
public abstract class BaseTask {
    private static final Logger LOG = LoggerFactory.getLogger(BaseTask.class);

    private ScheduledExecutorService executorService;

    public BaseTask() {
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * 启动该作业
     * @param hour  时间-小时
     * @param minute    时间-分钟
     * @param period  间隔 以天为单位
     */
    public void start(int day, int hour, int minute, int period) {
        long intervalMils;
        if (day == 0) {
            intervalMils = TimeUtils.getTimeInterval(hour, minute);
        } else {
            intervalMils = TimeUtils.getTimeInterval(day, hour, minute);
        }
        long periodMils = TimeUnit.DAYS.toMillis(period);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    process();
                } catch (Exception e) {
                    LOG.error("start task {} error", taskName(), e);
                }
            }
        }, intervalMils, periodMils, TimeUnit.MILLISECONDS);

        long[] allTimes = TimeUtils.transformMils(intervalMils);
        LOG.info("start task {} in {} hour {} minute {} seconds", taskName(), allTimes[0], allTimes[1], allTimes[2]);
    }

    /**
     * 立刻启动作业
     */
    public void startNow() {
        ExecutorService singleExecutor = Executors.newSingleThreadExecutor();
        singleExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            process();
                        } catch (Exception e) {
                            LOG.error("start now task {} error", taskName(), e);
                        }
                    }
                }
        );

        LOG.info("start task {} right now", taskName());
    }

    /**
     * task名称
     * @return
     */
    public abstract String taskName();

    /**
     * 执行具体作业的函数
     */
    abstract void process() throws Exception;
}
