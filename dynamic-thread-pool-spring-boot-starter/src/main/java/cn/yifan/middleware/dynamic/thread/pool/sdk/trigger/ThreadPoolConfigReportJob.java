package cn.yifan.middleware.dynamic.thread.pool.sdk.trigger;

import cn.yifan.middleware.dynamic.thread.pool.sdk.core.IDynamicThreadPoolService;
import cn.yifan.middleware.dynamic.thread.pool.sdk.entity.ThreadPoolConfigEntity;
import cn.yifan.middleware.dynamic.thread.pool.sdk.registry.IReporter;
import com.alibaba.fastjson.JSON;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * @FileName ThreadPoolConfigReportJob
 * @Description
 * @Author yifan
 * @date 2024-08-18 09:51
 **/
public class ThreadPoolConfigReportJob {

    private Logger logger = LoggerFactory.getLogger(ThreadPoolConfigReportJob.class);

    private IDynamicThreadPoolService dynamicThreadPoolService;

    private IReporter reporter;

    public ThreadPoolConfigReportJob(IDynamicThreadPoolService dynamicThreadPoolService, IReporter reporter) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.reporter = reporter;
    }

    @Scheduled(cron = "0/20 * * * * ?")
    public void executeReportThreadPoolConfig() {
        List<ThreadPoolConfigEntity> threadPoolConfigList = dynamicThreadPoolService.getThreadPoolConfigList();
        reporter.reportThreadPoolConfigList(threadPoolConfigList);
        logger.info("动态线程池，上报线程池列表信息：{}", JSON.toJSONString(threadPoolConfigList));

        for (ThreadPoolConfigEntity threadPoolConfigEntity : threadPoolConfigList) {
            reporter.reportThreadPoolConfigSingle(threadPoolConfigEntity);
            logger.info("动态线程池，上报线程池信息：{}", JSON.toJSONString(threadPoolConfigEntity));
        }
    }

}
