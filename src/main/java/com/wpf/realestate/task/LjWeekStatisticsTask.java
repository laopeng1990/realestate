package com.wpf.realestate.task;

import com.wpf.realestate.extractor.LjExtractor;
import com.wpf.realestate.storage.HouseRedisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wenpengfei on 2017/1/23.
 *
 * 周统计数据
 */
@Service
public class LjWeekStatisticsTask extends BaseTask {

    private LjExtractor ljExtractor;

    @Autowired
    public LjWeekStatisticsTask(HouseRedisDao houseRedisDao) {
        ljExtractor = new LjExtractor(houseRedisDao);
    }

    @Override
    public String taskName() {
        return "ljWeekStatisticsTask";
    }

    @Override
    public void process() {
        ljExtractor.runWeekStatistics();
    }
}
