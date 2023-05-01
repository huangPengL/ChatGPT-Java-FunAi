package com.gzhu.funai.utils;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/15 23:47
 */

@SpringBootTest
public class SnowflakeIdGeneratorTest {

    @Test
    public void test1(){
        int n = 10;
        for(int i=0;i<n;i++) {
            long l = SnowflakeIdGenerator.nextId();
            System.out.println(l);
        }
    }

}
