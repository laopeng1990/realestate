package com.wpf.realestate.task;

import com.wpf.realestate.extractor.LjHouseExtractor;
import com.wpf.realestate.storage.HouseRedisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by laopengwork on 2016/10/30.
 */
@Service
public class LjTask extends BaseTask {

    private LjHouseExtractor ljExtractor;

    @Autowired
    public LjTask(HouseRedisDao houseRedisDao) {
        ljExtractor = new LjHouseExtractor(houseRedisDao);
    }

    @Override
    public String taskName() {
        return "ljTask";
    }

    @Override
    public void process() {
        ljExtractor.run();
    }
}
