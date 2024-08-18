package cn.yifan.middleware.dynamic.thread.pool.sdk.core;

import cn.yifan.middleware.dynamic.thread.pool.sdk.entity.ThreadPoolConfigEntity;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @FileName DynamicThreadPoolService
 * @Description 核心服务实现类
 * @Author yifan
 * @date 2024-08-18 08:15
 **/
public class DynamicThreadPoolService implements IDynamicThreadPoolService {

    private Logger logger = LoggerFactory.getLogger(DynamicThreadPoolService.class);

    private String applicationName;

    private Map<String, ThreadPoolExecutor> threadPoolExecutorMap;

    public DynamicThreadPoolService(String applicationName, Map<String, ThreadPoolExecutor> threadPoolExecutorMap) {
        this.applicationName = applicationName;
        this.threadPoolExecutorMap = threadPoolExecutorMap;
    }

    private static ThreadPoolConfigEntity getThreadPoolConfigEntity(ThreadPoolExecutor threadPoolExecutor, String applicationName, String threadPoolName) {
        if (threadPoolExecutor == null) { return null; }
        ThreadPoolConfigEntity threadPoolConfigEntity = new ThreadPoolConfigEntity(applicationName, threadPoolName);
        // 最大线程数
        threadPoolConfigEntity.setCorePoolSize(threadPoolExecutor.getCorePoolSize());
        // 核心线程数
        threadPoolConfigEntity.setMaximumPoolSize(threadPoolExecutor.getMaximumPoolSize());
        // 活跃线程数
        threadPoolConfigEntity.setActiveCount(threadPoolExecutor.getActiveCount());
        // 阻塞队列类型
        threadPoolConfigEntity.setQueueType(threadPoolExecutor.getQueue().getClass().getSimpleName());
        // 阻塞队列总长度
        threadPoolConfigEntity.setQueueSize(threadPoolExecutor.getQueue().size());
        // 阻塞队列当前任务数
        threadPoolConfigEntity.setRemainingCapacity(threadPoolExecutor.getQueue().remainingCapacity());

        return threadPoolConfigEntity;
    }

    @Override
    public ThreadPoolConfigEntity getThreadPoolConfigByName(String threadPoolBeanName) {
        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolBeanName);
        if (threadPoolExecutor == null) { return new ThreadPoolConfigEntity(applicationName, threadPoolBeanName); }

        ThreadPoolConfigEntity threadPoolConfigVO = getThreadPoolConfigEntity(threadPoolExecutor, applicationName, threadPoolBeanName);

        if (logger.isDebugEnabled()) {
            logger.info("动态线程池，配置查询 应用名:{} 线程名:{} 池化配置:{}", applicationName, threadPoolBeanName, JSON.toJSONString(threadPoolConfigVO));
        }

        return threadPoolConfigVO;
    }

    @Override
    public List<ThreadPoolConfigEntity> getThreadPoolConfigList() {
        Set<String> threadPoolBeanNames = threadPoolExecutorMap.keySet();
        List<ThreadPoolConfigEntity> threadPoolConfigVOS = new ArrayList<>(threadPoolBeanNames.size());

        for (String threadPoolBeanName : threadPoolBeanNames) {
            ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolBeanName);
            ThreadPoolConfigEntity threadPoolConfigVO = getThreadPoolConfigEntity(threadPoolExecutor, applicationName, threadPoolBeanName);
            threadPoolConfigVOS.add(threadPoolConfigVO);
        }

        return threadPoolConfigVOS;
    }

    @Override
    public boolean updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity) {
        if (threadPoolConfigEntity == null || !applicationName.equals(threadPoolConfigEntity.getAppName())) { return false; }
        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolConfigEntity.getThreadPoolName());
        if (threadPoolExecutor == null) { return false; }
        logger.info("更新参数: {}", JSON.toJSONString(threadPoolConfigEntity));
        // 更新参数
        threadPoolExecutor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
        threadPoolExecutor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());

        return true;
    }
}
