package com.gzhu.funai.config;

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.injector.LogicSqlInjector;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;


import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/9 10:41
 */

@Configuration
@EnableTransactionManagement
@Slf4j
public class FunAiConfig {

    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    /**
     * 异步线程池
     * @return
     */
    @Bean(name = "queueThreadPool")
    public TaskExecutor queueThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(15);
        // 设置最大线程数
        executor.setMaxPoolSize(1024);
        // 设置队列容量
        executor.setQueueCapacity(100);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(100);
        // 设置默认线程名称
        executor.setThreadNamePrefix("async-service-");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        log.info("创建一个线程池 corePoolSize is [" + executor.getCorePoolSize() + "]" +
                " maxPoolSize is [" + executor.getMaxPoolSize() + "] " +
                " queueCapacity is [" + 100 + "]" +
                " keepAliveSeconds is [" + executor.getKeepAliveSeconds() + "]" +
                " namePrefix is [" + executor.getThreadNamePrefix() + "].");
        return executor;
    }

    /**
     * 逻辑删除
     * @return
     */
    @Bean
    public ISqlInjector sqlInjector(){
        return new LogicSqlInjector();
    }
}
