package cn.yifan;

import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @FileName ThreadPoolTest
 * @Description
 * @Author yifan
 * @date 2024-08-17 10:13
 **/

public class ThreadPoolTest {
    @Test
    public void threadPoolTest() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                3, 10, 1L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(10), new ThreadPoolExecutor.AbortPolicy()
        );

        for (int i = 0; i < 50; i++) {
            executor.execute(() -> {
                System.out.println(Thread.currentThread().getName() + ", 执行任务");

            });
        }
    }
}
