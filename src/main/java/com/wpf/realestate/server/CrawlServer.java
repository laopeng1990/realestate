package com.wpf.realestate.server;

import com.google.common.util.concurrent.AbstractIdleService;
import com.wpf.realestate.common.GlobalConfig;
import com.wpf.realestate.data.TaskConfig;
import com.wpf.realestate.task.BaseTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.Map;

/**
 * Created by laopengwork on 2016/10/30.
 */
public class CrawlServer extends AbstractIdleService {
    private static final Logger LOG = LoggerFactory.getLogger(CrawlServer.class);

    private FileSystemXmlApplicationContext context;

    @Override
    protected void startUp() throws Exception {
        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext();
        context.setValidating(false);
        context.setConfigLocation("config/beans.xml");
        context.refresh();

        //启动tasks
        String[] taskNames = context.getBeanNamesForType(BaseTask.class);
        Map<String, TaskConfig> taskConfigMap = GlobalConfig.getTaskConfigMap();
        if (taskNames != null) {
            for (String name : taskNames) {
                BaseTask task = context.getBean(name, BaseTask.class);
                TaskConfig config = taskConfigMap.get(task.taskName());
                if (config == null) {
                    LOG.warn("no config for task {}", task.taskName());
                    continue;
                }
                if (!config.isEnable()) {
                    continue;
                }
                if (config.isStartNow()) {
                    task.startNow();
                }
                task.start(config.getDay(), config.getHour(), config.getMinute(), config.getPeriod());
            }
        }

        addShutdownHooks();
    }

    private void addShutdownHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            CrawlServer.this.shutDown();
                        } catch (Exception e) {
                            LOG.error("Error while shutting down", e);
                        }
                    }
                }
        ));
    }

    @Override
    protected void shutDown() throws Exception {
        LOG.warn("shutting down crawl server");
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        LOG.info("starting crawl server");

        CrawlServer crawlServer = new CrawlServer();
        crawlServer.startAsync().awaitRunning();

        LOG.info("started crawl server int {} mils", System.currentTimeMillis() - start);
    }

}
