package org.example;

import com.google.common.util.concurrent.RateLimiter;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class App {

    public static int QPS = 200;
    public static int QUERY_COUNT = 10000;

    public static void main(String[] args) throws Exception {
        // funcTest();
        // funcTestOrigin();
        stressTest(QPS, QUERY_COUNT);
        // mulStressTest(QPS, QUERY_COUNT, 3);
        // maxQpsTest();
    }

    // 本地Tomcat功能测试
    public static void funcTest() throws IOException {
        int count = 0;
        for (int i = 0; i < 10000; i++) {
            System.out.println("第" + i + "次调用:");
            boolean success = HttpClientUtil.sendRequest();
            if (success) {
                count++;
            }
        }
        System.out.println("成功率:" + count / 10000.0);
    }

    // 云服务器功能测试
    public static void funcTestOrigin() throws IOException {
        int count = 0;
        for (int i = 0; i < 10000; i++) {
            System.out.println("第" + i + "次调用:");
            boolean success = HttpClientUtil.sendOriginRequest();
            if (success) {
                count++;
            }
        }
        System.out.println("成功率:" + count / 10000.0);
    }

    public static void maxQpsTest() throws InterruptedException {
        int qps = 100;
        int totalCount = 10000;
        while (stressTest(qps, totalCount) > 0.9995) {
            qps += 50;
        }
        System.out.println("最大QPS: " + qps);
    }

    public static void mulStressTest(int qps, int totalCount, int num) throws InterruptedException {
        double avgSuccess = 0;
        for (int i = 0; i < num; i++) {
            double success = stressTest(qps, totalCount);
            avgSuccess += success;
        }
        System.out.println("QPS=" + qps + "的平均成功率: " + avgSuccess / num);
    }

    public static double stressTest(int qps, int totalCount) throws InterruptedException {
        // int qps = 100;
        int threadCount = qps / 5;
        // 10000次测试中，若要满足99.95%可用性，只能失败5次
        // int totalCount = 10000;
        int duration = totalCount / qps + 1;

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger countRequest = new AtomicInteger(0);
        AtomicLong startTimeMillis = new AtomicLong();

        // 初始化RateLimiter，设置为每秒qps个令牌
        final RateLimiter rateLimiter = RateLimiter.create(qps);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // 使用CountDownLatch确保所有线程准备就绪
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 线程等待启动信号
                    startSignal.await();

                    long endTime = System.currentTimeMillis() + duration * 1000;

                    while (System.currentTimeMillis() <= endTime && countRequest.get() < totalCount) {
                        rateLimiter.acquire();
                        try {
                            boolean success = HttpClientUtil.sendRequest();
                            // boolean success = HttpClientUtil.sendOriginRequest();
                            if (success) {
                                successCount.incrementAndGet();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        countRequest.incrementAndGet();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneSignal.countDown(); // 任务完成，计数减一
                }
            });
        }

        // 记录开始时间，并发出启动信号
        startTimeMillis.set(System.currentTimeMillis());
        startSignal.countDown(); // 允许所有线程开始执行

        // 等待所有线程完成
        doneSignal.await();
        executorService.shutdown();

        System.out.println("execute time: " + (System.currentTimeMillis() - startTimeMillis.get()) / 1000);
        System.out.println("Total requests sent: " + countRequest.get());
        System.out.println("Total successful requests: " + successCount.get());
        System.out.println("Success rate: " + successCount.get() / (double) countRequest.get());
        return successCount.get() / (double) countRequest.get();
    }

}
