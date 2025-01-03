package com.lb.types.common;

public class Constants {

    // 分隔符
    public final static String SPLIT = ",";
    // 冒号
    public final static String COLON = ":";
    // 空格
    public final static String SPACE = " ";
    public final static String UNDERLINE = "_";

    public static class RedisKey {
        // 策略键前缀
        public static String STRATEGY_KEY = "big_market_strategy_key_";
        // 策略奖励键前缀
        public static String STRATEGY_AWARD_KEY = "big_market_strategy_award_key_";
        // 策略费率表键前缀
        public static String STRATEGY_RATE_TABLE_KEY = "big_market_strategy_rate_table_key_";
        // 策略费率范围键前缀
        public static String STRATEGY_RATE_RANGE_KEY = "big_market_strategy_rate_range_key_";
    }

}
