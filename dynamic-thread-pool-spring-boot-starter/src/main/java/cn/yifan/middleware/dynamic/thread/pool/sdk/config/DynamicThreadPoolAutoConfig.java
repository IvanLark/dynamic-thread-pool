package cn.yifan.middleware.dynamic.thread.pool.sdk.config;

import cn.yifan.middleware.dynamic.thread.pool.sdk.constant.RedisConstant;
import cn.yifan.middleware.dynamic.thread.pool.sdk.core.DynamicThreadPoolService;
import cn.yifan.middleware.dynamic.thread.pool.sdk.core.IDynamicThreadPoolService;
import cn.yifan.middleware.dynamic.thread.pool.sdk.entity.ThreadPoolConfigEntity;
import cn.yifan.middleware.dynamic.thread.pool.sdk.registry.IReporter;
import cn.yifan.middleware.dynamic.thread.pool.sdk.registry.redis.RedisReporter;
import cn.yifan.middleware.dynamic.thread.pool.sdk.trigger.ThreadPoolConfigAdjustListener;
import cn.yifan.middleware.dynamic.thread.pool.sdk.trigger.ThreadPoolConfigReportJob;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @FileName DynamicThreadPoolAutoConfig
 * @Description 动态配置入口
 * @Author yifan
 * @date 2024-08-16 17:41
 **/

@Configuration
@EnableConfigurationProperties(DynamicThreadPoolAutoProperties.class)
// 开启定时任务
@EnableScheduling
public class DynamicThreadPoolAutoConfig {

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);

    private String applicationName;

    @Bean("dynamicThreadPoolRedissonClient")
    public RedissonClient redissonClient(DynamicThreadPoolAutoProperties properties) {
        // 配置
        Config config = new Config();
        // 根据需要可以设定编解码器；https://github.com/redisson/redisson/wiki/4.-%E6%95%B0%E6%8D%AE%E5%BA%8F%E5%88%97%E5%8C%96
        config.setCodec(JsonJacksonCodec.INSTANCE);

        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setPassword(properties.getPassword())
                .setConnectionPoolSize(properties.getPoolSize())
                .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                .setIdleConnectionTimeout(properties.getIdleTimeout())
                .setConnectTimeout(properties.getConnectTimeout())
                .setRetryAttempts(properties.getRetryAttempts())
                .setRetryInterval(properties.getRetryInterval())
                .setPingConnectionInterval(properties.getPingInterval())
                .setKeepAlive(properties.isKeepAlive());

        RedissonClient redissonClient = Redisson.create(config);

        logger.info("动态线程池，注册器（redis）链接初始化完成。{} {} {}", properties.getHost(), properties.getPoolSize(), !redissonClient.isShutdown());

        return redissonClient;
    }

    @Bean
    public RedisReporter redisReporter(RedissonClient redissonClient) {
        return new RedisReporter(redissonClient);
    }

    @Bean
    public DynamicThreadPoolService dynamicThreadPoolService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutorMap, RedissonClient redissonClient) {
        applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");
        if (StringUtils.isBlank(applicationName)) {
            applicationName = "缺省的";
            logger.warn("动态线程池，启动提示。SpringBoot 应用未配置 spring.application.name 无法获取到应用名称！");
        }

        // 将本地线程池配置更新为注册中心中的配置
        // 就是说如果服务重启，里面的线程池配置应该和注册中心中的配置同步
        Set<String> threadPoolBeanNames = threadPoolExecutorMap.keySet();
        for (String threadPoolBeanName : threadPoolBeanNames) {
            // 从注册中心获取线程池配置
            String key = String.format("%s:%s:%s",
                    RedisConstant.THREAD_POOL_CONFIG_SINGLE_KEY,
                    applicationName,
                    threadPoolBeanName
            );
            ThreadPoolConfigEntity threadPoolConfigEntity = redissonClient.<ThreadPoolConfigEntity>getBucket(key).get();
            if (threadPoolConfigEntity == null) { continue; }

            // 更新本地线程池配置
            ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolBeanName);
            threadPoolExecutor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
            threadPoolExecutor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
        }

        logger.info("动态线程池Service服务注册完成, 当前线程池: {}", JSON.toJSONString(threadPoolExecutorMap.keySet()));

        return new DynamicThreadPoolService(applicationName, threadPoolExecutorMap);
    }

    @Bean
    public ThreadPoolConfigReportJob threadPoolConfigReportJob(IDynamicThreadPoolService dynamicThreadPoolService, IReporter reporter) {
        return new ThreadPoolConfigReportJob(dynamicThreadPoolService, reporter);
    }

    @Bean
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(IDynamicThreadPoolService dynamicThreadPoolService, IReporter reporter, RedissonClient redissonClient) {
        ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener = new ThreadPoolConfigAdjustListener(dynamicThreadPoolService, reporter);
        // 绑定Listener
        logger.info("绑定listener");
        String key = String.format("%s:%s", RedisConstant.THREAD_POOL_CONFIG_UPDATE_TOPIC, applicationName);
        RTopic topic = redissonClient.getTopic(key);
        topic.addListener(ThreadPoolConfigEntity.class, threadPoolConfigAdjustListener);

        return threadPoolConfigAdjustListener;
    }

}
