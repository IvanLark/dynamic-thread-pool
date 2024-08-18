package cn.yifan.middleware.dynamic.thread.pool.sdk.registry.redis;

import cn.yifan.middleware.dynamic.thread.pool.sdk.constant.RedisConstant;
import cn.yifan.middleware.dynamic.thread.pool.sdk.entity.ThreadPoolConfigEntity;
import cn.yifan.middleware.dynamic.thread.pool.sdk.registry.IReporter;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.List;

/**
 * @FileName RedisReporter
 * @Description 线程池信息上报 Redis 实现类
 * @Author yifan
 * @date 2024-08-18 09:15
 **/
public class RedisReporter implements IReporter {

    private final RedissonClient redissonClient;

    public RedisReporter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void reportThreadPoolConfigList(List<ThreadPoolConfigEntity> threadPoolEntities) {
        RList<ThreadPoolConfigEntity> list = redissonClient.getList(RedisConstant.THREAD_POOL_CONFIG_LIST_KEY);
        list.delete();
        list.addAll(threadPoolEntities);
    }

    @Override
    public void reportThreadPoolConfigSingle(ThreadPoolConfigEntity threadPoolConfigEntity) {

        String key = String.format("%s:%s:%s",
                RedisConstant.THREAD_POOL_CONFIG_SINGLE_KEY,
                threadPoolConfigEntity.getAppName(),
                threadPoolConfigEntity.getThreadPoolName()
        );
        RBucket<ThreadPoolConfigEntity> bucket = redissonClient.getBucket(key);
        bucket.set(threadPoolConfigEntity, Duration.ofDays(30L));
    }
}
