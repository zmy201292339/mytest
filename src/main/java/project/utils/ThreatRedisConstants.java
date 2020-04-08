package project.utils;

public interface ThreatRedisConstants {
    /**
     * 雪花获取id加锁
     */
    String LOCK_SNOWFLAKE = "lock:snowflake";

    /**
     * 设备查询ioc记录加锁
     */
    String LOCK_DEVICE_IOC = "lock:device:ioc:";

    /**
     * 雪花业务id
     */
    String SNOWFLAKE_ID_BUSINESS = "snowflake:id:business";

    /**
     * 雪花机器id
     */
    String SNOWFLAKE_ID_MACHINE = "snowflake:id:machine";
}
