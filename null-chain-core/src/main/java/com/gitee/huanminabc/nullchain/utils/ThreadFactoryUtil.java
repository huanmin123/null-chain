package com.gitee.huanminabc.nullchain.utils;


import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.enums.ThreadMotiveEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 线程池
 *
 * @author huanmin
 * @date 2023/11/21
 */
@Slf4j
public class ThreadFactoryUtil {
    private static final ConcurrentHashMap<String, ThreadPoolExecutor> executorMap= new ConcurrentHashMap<>();  ;
    public static final String DEFAULT_THREAD_FACTORY_NAME = "$$$--DEFAULT_THREAD_FACTORY--$$$";

    //获取cpu核心数
//    对于CPU密集型任务，CORE_POOL_SIZE可以设置为CPU核心数的两倍左右。这是因为CPU密集型任务主要依赖CPU的计算能力，设置过多的线程反而会因为线程切换和调度开销而降低效率。
//    对于I/O密集型任务，由于线程经常需要等待I/O操作完成，因此可以设置更多的线程以提高CPU利用率。CORE_POOL_SIZE可以适当增大，但具体数值需要根据I/O等待时间和CPU处理能力来权衡。
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    //现在栈大小都是1M, 那么假设你空闲内存有10G,那么最大线程数就是10G/1M=10000 ,但是一般来说5000就够了,其他程序也需要内存, 到了这个峰值那么就需要考虑
    //是否是逻辑问题,如果是逻辑问题,那么就需要考虑是否需要优化,如果不是逻辑问题,那么就需要考虑是否需要增加机器了, 官方建议jvm最大内存使用30G左右是极限了在多也没用,只会越来越慢
    // 而这30G除非就你一个程序,理论上线程池的最大线程数就是30G/1M=30000,但是不现实, 因为还有操作系统本身的各种线程和应用
    // 一般来说单台机器5000那么就是极限了, 这里说的是顶配机器, 如果是普通机器,那么就更少了, 一般来说1024就够了
    //如果单业务超出队列后还创建了1024次线程,那么就会报错,代码肯定有问题,要不就是该加服务器了,所以这里限制一下
    private final static int LIMIT_MAXIMUM_POOL_SIZE = 1024;

    //单个业务默认线程池大小
    private final static int DEFAULT_MAX_POOL_SIZE = 128;
    //单个业务线程池默认任务数量
    private final static int DEFAULT_TASK_NUM = 1024;
    //全局默认的线程池大小
    private final static int ALL_DEFAULT_MAX_POOL_SIZE = 1024;
    //全局默认的线程池任务数量
    private final static int ALL_DEFAULT_TASK_NUM = 10000;

    static {
        ThreadFactoryUtil.addExecutor(ThreadFactoryUtil.DEFAULT_THREAD_FACTORY_NAME,ALL_DEFAULT_MAX_POOL_SIZE,ALL_DEFAULT_TASK_NUM,ThreadMotiveEnum.CPU); //初始化线程池
    }

    //获取默认线程池
    public static ThreadPoolExecutor getDefaultExecutor() {
        return executorMap.get(DEFAULT_THREAD_FACTORY_NAME);
    }


    //添加指定key的线程池
    public static void addExecutor(String threadFactoryName, int maxPoolSize, int taskNum, ThreadMotiveEnum type) {
        executorMap.computeIfAbsent(threadFactoryName, k -> create(threadFactoryName, maxPoolSize, taskNum, type));
    }
    public static void addExecutor(String threadFactoryName, int maxPoolSize, int taskNum) {
        addExecutor(threadFactoryName, maxPoolSize, taskNum, ThreadMotiveEnum.CPU);//默认是cpu密集型
    }
    //添加默认线程池
    //默认是cpu密集型,除非你在类中自己声明添加了线程池,否则默认是cpu密集型
    public static void addExecutor(String threadFactoryName) {
        addExecutor(threadFactoryName, DEFAULT_MAX_POOL_SIZE, DEFAULT_TASK_NUM, ThreadMotiveEnum.CPU);
    }

    //删除指定key的线程池
    public static void removeExecutor(String threadFactoryName) {
        ThreadPoolExecutor remove = executorMap.remove(threadFactoryName);
        if (remove != null) {
            remove.shutdown();
        }
    }


    public static ThreadPoolExecutor getExecutor(String threadFactoryName) {
      return   executorMap.computeIfAbsent(threadFactoryName, k -> create(threadFactoryName, DEFAULT_MAX_POOL_SIZE, DEFAULT_TASK_NUM, ThreadMotiveEnum.CPU));
    }

    //判断是否存在
    public static boolean containsExecutor(String threadFactoryName) {
        return executorMap.containsKey(threadFactoryName);
    }

    // threadPoolName: 线程池名称
    // 任务数量,建议任务数量要比实际统计的任务数量大一些,因为会作为队列的容量
    private static ThreadPoolExecutor create(String threadNamePrefix, int maxPoolSize, int taskNum, ThreadMotiveEnum type) {

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat(threadNamePrefix + "-%d").build();

        //如果是io密集型,那么核心线程数就是最大线程数,这样可以避免线程的创建和销毁,提高性能
        //如果是cpu密集型,那么核心线程数就是cpu核心数的两倍,这样可以提高cpu的利用率
        int corePoolSize = CORE_POOL_SIZE;
        //判断不能大于LIMIT_MAXIMUM_POOL_SIZE
        if (maxPoolSize > LIMIT_MAXIMUM_POOL_SIZE) {
            maxPoolSize = LIMIT_MAXIMUM_POOL_SIZE;
        }
        int newMaxPoolSize = Math.max(maxPoolSize, CORE_POOL_SIZE * 2);
        if (type.equals(ThreadMotiveEnum.IO)) {
            corePoolSize = maxPoolSize;
            newMaxPoolSize = maxPoolSize * 2; //io密集的线程需要大一些,因为他们大部分时间都是等待io,所以需要更多的线程
            taskNum = newMaxPoolSize * 2+taskNum; //任务队列大小,防止最大线程不够用
        }
        // 创建线程池，其中任务队列需要结合实际情况设置合理的容量
        return new ThreadPoolExecutor(
                corePoolSize,
                newMaxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(Math.max(taskNum, DEFAULT_TASK_NUM)),
                namedThreadFactory,
                new MyNewThreadRunsPolicy(threadNamePrefix));
    }


    //拒绝策略 ,直接在调用者线程中创建, 为什要这样做,因为业务场景不好预估,可能出现浮动,或者说是突发的高并发
    // 如果不这样做,那么就会出现线程池任务队列满了,但是线程池也满了,那么就会抛出异常,导致任务失败
    //解决办法就是记录error日志,然后在调用者线程中创建,这样就不会出现任务失败的情况
    //之后可以根据error日志,来调整线程池的大小, 最好加个监控,监控指定日志,如果发现了,就及时修改
    //后期可以做成动态的,替换Map里的线程池,这样就不用重启项目了
    public static class MyNewThreadRunsPolicy implements RejectedExecutionHandler {
        private  int count = 0;
        private final String threadNamePrefix;
        // 创建ThreadMXBean对象

        public MyNewThreadRunsPolicy(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-full" + "-%d").build();
                Thread thread = namedThreadFactory.newThread(r);
                thread.start();
                synchronized (this) {
                    count++;
                    if (count > LIMIT_MAXIMUM_POOL_SIZE) {
                        throw new NullChainException("线程池和任务队列满了,并且额外创建了" + count + "个线程,请检查是否有逻辑问题,如果没有,请增加线程池大小或者增加服务器");
                    }
                }
                log.warn("######ThreadFull######{}线程池任务队列已满，当前线程数：{}，任务数：{}，拒绝策略：{}，不通过线程池已新创建线程数(总数)：{}",
                        threadNamePrefix,
                        e.getPoolSize(),
                        e.getQueue().size(),
                        e.getRejectedExecutionHandler().getClass().getSimpleName(),
                        count
                );


            }
        }
    }

    public static StackTraceElement currentStackTrace(int num) {
        return StackTraceUtil.currentStackTrace(num);
    }


}
