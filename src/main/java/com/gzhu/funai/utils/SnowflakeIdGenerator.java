package com.gzhu.funai.utils;

/**
 * 雪花算法用于生成用户ID
 * @Author: huangpenglong
 * @Date: 2023/3/15 23:44
 */
public class SnowflakeIdGenerator {
    // 数据中心ID，可以根据实际情况修改
    private static long datacenterId = 1L;
    // 机器标识ID，可以根据实际情况修改
    private static long machineId = 1L;
    // 序列号
    private static long sequence = 0L;
    // 上一次生成ID的时间戳
    private static long lastTimestamp = -1L;
    // 数据中心ID位数
    private static long datacenterIdBits = 5L;
    // 机器标识ID位数
    private static long machineIdBits = 5L;
    // 序列号位数
    private static long sequenceBits = 12L;
    // 时间戳左移位数
    private static long timestampLeftShift = sequenceBits + machineIdBits + datacenterIdBits;
    // 数据中心ID左移位数
    private static long datacenterIdLeftShift = sequenceBits + machineIdBits;
    // 机器标识ID左移位数
    private static long machineIdLeftShift = sequenceBits;
    // 最大序列号
    private static long maxSequence = ~(-1L << sequenceBits);

    public static long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards, refusing to generate id");
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - 1609459200000L) << timestampLeftShift) |
                (datacenterId << datacenterIdLeftShift) |
                (machineId << machineIdLeftShift) |
                sequence;
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }


    private SnowflakeIdGenerator(){}
}
