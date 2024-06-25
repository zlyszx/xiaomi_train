package org.example.sqrtdemo.service;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncService {

    @Async
    public CompletableFuture<Double> calculateSqrtAsync(int number) throws InterruptedException {

        NormalDistribution normal = new NormalDistribution(150, 12);
        // 生成一个随机休眠时间
        double randomNum = normal.sample();
        Thread.sleep((int) randomNum);

        double sqrtResult = Math.sqrt(number);
        return CompletableFuture.completedFuture(sqrtResult);
    }

}
