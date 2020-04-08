package project.utils;

import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class IdGenerator {

    /**
     * 业务id和机器id使用缓存管理
     * 缓存key为区分业务/机器，value为hash存储数据
     * hash key为id，hash value为hostname
     */
    @Resource
    private RedisTemplate<String, HashMap<String, String>> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private String applicationName = "unknown";

    /**
     * 雪花中数据中心id，这里使用业务id
     */
    private Long businessIdId;   //数据id

    /**
     * 机器id
     */
    private Long workerId;    //工作id

    //12位的序列号
    private long sequence = 1;
    //初始时间戳
    private long initialTime = 1584720000000L;

    //长度为5位
    private long workerIdBits = 5L;
    private long businessIdIdBits = 5L;
    //最大值
    private long maxWorkerId = ~(-1L << workerIdBits);
    private long maxBusinessId = ~(-1L << businessIdIdBits);
    //序列号id长度
    private long sequenceBits = 12L;
    //序列号最大值
    private long sequenceMask = ~(-1L << sequenceBits);

    //工作id需要左移的位数，12位
    private long workerIdShift = sequenceBits;
    //数据id需要左移位数 12+5=17位
    private long businessIdIdShift = sequenceBits + workerIdBits;
    //时间戳需要左移位数 12+5+5=22位
    private long timestampLeftShift = sequenceBits + workerIdBits + businessIdIdBits;

    //上次时间戳，初始值为负数
    private long lastTimestamp = -1L;

    /**
     * 初始化业务id和机器id
     */
    @PostConstruct
    public void init(){
        if(StringUtils.isEmpty(this.applicationName)) {
            throw new IllegalArgumentException("spring application name must be not empty");
        }

        String businessName = applicationName;
        String machineName = CommonUtils.hostname().orElse("N/A") + "_" + CommonUtils.ipv4ListString();

        RLock lock = this.redissonClient.getLock(ThreatRedisConstants.LOCK_SNOWFLAKE);
        try {
            lock.lock(1, TimeUnit.MINUTES);

            // get id from redis
            businessIdId = this.findIdFromCache(ThreatRedisConstants.SNOWFLAKE_ID_BUSINESS, applicationName, maxBusinessId);
            workerId = this.findIdFromCache(ThreatRedisConstants.SNOWFLAKE_ID_MACHINE, machineName, maxWorkerId);

            // sanity check for workerId
            if (workerId == null || workerId > maxWorkerId || workerId < 0) {
                throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0",maxWorkerId));
            }
            if (businessIdId == null || businessIdId > maxBusinessId || businessIdId < 0) {
                throw new IllegalArgumentException(String.format("businessId Id can't be greater than %d or less than 0", maxBusinessId));
            }


            // save id
            HashOperations<String, String, String> hashOps = this.redisTemplate.opsForHash();
            hashOps.put(ThreatRedisConstants.SNOWFLAKE_ID_BUSINESS, businessIdId.toString(), businessName);
            hashOps.put(ThreatRedisConstants.SNOWFLAKE_ID_MACHINE, workerId.toString(), machineName);
        } finally {
            lock.unlock();
        }
    }

    private Long findIdFromCache(String redisKey, String idName, long maxId) {
        HashOperations<String, String, String> hashOps = this.redisTemplate.opsForHash();
        Cursor<Map.Entry<String, String>> scan = hashOps.scan(redisKey, ScanOptions.NONE);
        while (scan.hasNext()) {
            Map.Entry<String, String> next = scan.next();
            if(next.getValue().equals(idName)) {
                Long id = Long.valueOf(next.getKey());
                return id;
            }
        }
        for (int i = 0; i <= maxId; i++) {
            if(!hashOps.hasKey(redisKey, String.valueOf(i))) {
                return (long) i;
            }
        }
        return null;
    }

    @PreDestroy
    public void destroy() {
        this.releaseIds();
    }

    public void releaseIds() {
        HashOperations<String, String, String> hashOps = this.redisTemplate.opsForHash();
        if(businessIdId != null) {
            hashOps.delete(String.valueOf(businessIdId));
        }
        if(workerId != null) {
            hashOps.delete(String.valueOf(workerId));
        }
    }

    public long getTimestamp(){
        return System.currentTimeMillis();
    }

    //下一个ID生成算法
    public synchronized long nextId() {
        long timestamp = timeGen();

        //获取当前时间戳如果小于上次时间戳，则表示时间戳获取出现异常
        if (timestamp < lastTimestamp) {
            System.err.printf("clock is moving backwards.  Rejecting requests until %d.", lastTimestamp);
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
                    lastTimestamp - timestamp));
        }

        //获取当前时间戳如果等于上次时间戳（同一毫秒内），则在序列号加一；否则序列号赋值为0，从0开始。
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        //将上次时间戳值刷新
        lastTimestamp = timestamp;

        /*
         * 返回结果：
         * (timestamp - initialTime) << timestampLeftShift) 表示将时间戳减去初始时间戳，再左移相应位数
         * (businessIdId << businessIdIdShift) 表示将数据id左移相应位数
         * (workerId << workerIdShift) 表示将工作id左移相应位数
         * | 是按位或运算符，例如：x | y，只有当x，y都为0的时候结果才为0，其它情况结果都为1。
         * 因为个部分只有相应位上的值有意义，其它位上都是0，所以将各部分的值进行 | 运算就能得到最终拼接好的id
         */
        return ((timestamp - initialTime) << timestampLeftShift) |
                (businessIdId << businessIdIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    //获取时间戳，并与上次时间戳比较
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    //获取系统时间戳
    private long timeGen(){
        return System.currentTimeMillis();
    }
}

