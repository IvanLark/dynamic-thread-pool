package cn.yifan.middleware.dynamic.thread.pool.sdk.registry;

import cn.yifan.middleware.dynamic.thread.pool.sdk.entity.ThreadPoolConfigEntity;

import java.util.List;

/**
 * @FileName IReporter
 * @Description 线程池信息上报接口
 * @Author yifan
 * @date 2024-08-18 09:08
 **/
public interface IReporter {

    public void reportThreadPoolConfigList(List<ThreadPoolConfigEntity> threadPoolEntities);

    public void reportThreadPoolConfigSingle(ThreadPoolConfigEntity threadPoolConfigEntity);

}
