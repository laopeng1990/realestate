package com.wpf.realestate.task;

import com.wpf.realestate.extractor.LjExtractor;
import com.wpf.realestate.storage.HouseRedisDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by wenpengfei on 2016/12/22.
 */
public class LjStatisticsTask extends BaseTask {

    private LjExtractor ljExtractor;

    @Autowired
    public LjStatisticsTask(HouseRedisDao houseRedisDao) {
        ljExtractor = new LjExtractor(houseRedisDao);
    }

    @Override
    public String taskName() {
        return "ljStatisticTask";
    }

    @Override
    public void process() {
        ljExtractor.runStatistics();
    }
}
