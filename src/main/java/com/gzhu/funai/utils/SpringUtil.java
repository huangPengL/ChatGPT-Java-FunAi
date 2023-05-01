package com.gzhu.funai.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Spring ApplicationContext 工具类
 * @Author: huangpenglong
 * @Date: 2023/3/31 17:44
 */
@SuppressWarnings("unchecked")
@Component
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext){
        SpringUtil.applicationContext = applicationContext;
    }

    public static <T> T getBean(String beanName) {
        if(applicationContext.containsBean(beanName)){
            return (T) applicationContext.getBean(beanName);
        }else{
            return null;
        }
    }

    public static String[] getAllBeanName(){
        return applicationContext.getBeanDefinitionNames();
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> baseType){
        return applicationContext.getBeansOfType(baseType);
    }
}
