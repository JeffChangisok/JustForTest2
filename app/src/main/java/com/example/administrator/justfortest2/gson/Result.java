package com.example.administrator.justfortest2.gson;

import java.util.List;

/**
 * Created by Administrator on 2017/5/16.
 */

public class Result {
    public Hourly hourly;
    public Daily daily;

    public class Hourly{
        public List<HourlyTemp> temperature;
        public List<Skycon> skycon;
    }

    public class Daily{
        public List<DailyTemp> temperature;
        public List<Skycon> skycon;
    }

    public class Skycon {
        public String value;
    }
}
