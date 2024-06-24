package org.example.sqrtdemo.controller;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class SqrtController {

    /**
     * 功能：计算整型数据number的平方根，并模拟100-200ms的延迟
     * @param number
     * @return number小于0，返回-1；否则返回number的平方根
     * @throws InterruptedException
     */
    @GetMapping("/sqrt/{number}")
    public double calculateSqrt(@PathVariable("number") int number) throws InterruptedException {

        // System.out.println("number:" + number);

        // 均匀分布生成随机数，不合理
        // int randomNum = new Random().nextInt(100, 200);

        // 正态分布，均值为150，标准差12, 模拟延迟100-200的概率为0.99997
        NormalDistribution normal = new NormalDistribution(150, 12);

        // System.out.println("normal.cumulativeProbability(100):" + normal.cumulativeProbability(100));

        // 生成一个随机休眠时间
        double randomNum = normal.sample();

        // System.out.println("randomNum:" + randomNum);
        Thread.sleep((int) randomNum);
        // Thread.sleep(150);

        // 小于0的数，直接返回-1
        if (number < 0) {
            return -1;
        }
        double sqrtResult = Math.sqrt(number);
        // System.out.println("sqrtResult:" + sqrtResult);

        return sqrtResult;
    }
}
