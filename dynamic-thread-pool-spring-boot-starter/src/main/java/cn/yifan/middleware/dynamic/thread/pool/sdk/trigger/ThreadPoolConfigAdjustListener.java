package cn.yifan.middleware.dynamic.thread.pool.sdk.trigger;

import cn.yifan.middleware.dynamic.thread.pool.sdk.core.IDynamicThreadPoolService;
import cn.yifan.middleware.dynamic.thread.pool.sdk.entity.ThreadPoolConfigEntity;
import cn.yifan.middleware.dynamic.thread.pool.sdk.registry.IReporter;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @FileName ThreadPoolConfigAdjustListener
 * @Description 监听线程池配置更新事件
 * @Author yifan
 * @date 2024-08-18 09:51
 **/
public class ThreadPoolConfigAdjustListener implements MessageListener<ThreadPoolConfigEntity> {

    private final Logger logger = LoggerFactory.getLogger(ThreadPoolConfigAdjustListener.class);

    private final IDynamicThreadPoolService dynamicThreadPoolService;

    private final IReporter reporter;

    public ThreadPoolConfigAdjustListener(IDynamicThreadPoolService dynamicThreadPoolService, IReporter reporter) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.reporter = reporter;
    }

    @Override
    public void onMessage(CharSequence charSequence, ThreadPoolConfigEntity threadPoolConfigEntity) {
        logger.info("动态线程池，调整线程池配置。线程池名称:{} 核心线程数:{} 最大线程数:{}", threadPoolConfigEntity.getThreadPoolName(), threadPoolConfigEntity.getPoolSize(), threadPoolConfigEntity.getMaximumPoolSize());
        dynamicThreadPoolService.updateThreadPoolConfig(threadPoolConfigEntity);

        // 更新后上报最新数据
        List<ThreadPoolConfigEntity> threadPoolConfigList = dynamicThreadPoolService.getThreadPoolConfigList();
        reporter.reportThreadPoolConfigList(threadPoolConfigList);
        threadPoolConfigEntity = dynamicThreadPoolService.getThreadPoolConfigByName(threadPoolConfigEntity.getThreadPoolName());
        reporter.reportThreadPoolConfigSingle(threadPoolConfigEntity);
    }

}
