package com.wpf.realestate.task;

import com.wpf.realestate.extractor.LjStatisticsExtractor;
import com.wpf.realestate.storage.HouseRedisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wenpengfei on 2016/12/22.
 */
@Service
public class LjStatisticsTask extends BaseTask {

    private LjStatisticsExtractor ljExtractor;

    @Autowired
    public LjStatisticsTask(HouseRedisDao houseRedisDao) {
        ljExtractor = new LjStatisticsExtractor(houseRedisDao);
    }

    @Override
    public String taskName() {
        return "ljStatisticsTask";
    }

    @Override
    public void process() {
        ljExtractor.run();
    }
}
