package cn.yifan.middleware.dynamic.thread.pool.sdk.core;

import cn.yifan.middleware.dynamic.thread.pool.sdk.entity.ThreadPoolConfigEntity;
import java.util.List;

/**
 * @FileName IDynamicThreadPoolService
 * @Description 核心服务接口
 * @Author yifan
 * @date 2024-08-18 07:59
 **/
public interface IDynamicThreadPoolService {

    public ThreadPoolConfigEntity getThreadPoolConfigByName(String threadPoolBeanName);

    public List<ThreadPoolConfigEntity> getThreadPoolConfigList();

    public boolean updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity);

}
