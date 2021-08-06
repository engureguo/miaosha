package com.engure.miaosha.controller;

import com.engure.miaosha.service.StockService;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("ms")
public class MiaoshaController {

    @Autowired
    private StockService stockService;

    // 创建令牌桶实例
    private RateLimiter rateLimiter = RateLimiter.create(50);//每秒产生多少个token

    @GetMapping("limiter")
    public String limiter(Integer id) {
        //1.阻塞式
        //double acqDuration = rateLimiter.acquire();//获取令牌阻塞了多少秒
        //System.out.println(" 等待了 " + acqDuration + " s");

        //2.超时等待
        boolean pass = rateLimiter.tryAcquire(2, TimeUnit.SECONDS);
        if (!pass) {
            System.out.println("当前请求被限流，直接被抛弃...");
            return "请重试.";
        }
        System.out.println("处理业务.............");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "令牌桶测试";
    }

    @GetMapping("kill")
    public String kill(Integer id) {

        try {
            int orderId = stockService.kill(id);
            return "秒杀成功！，订单编号 " + orderId;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }

    }

}
