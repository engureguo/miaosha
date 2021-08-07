package com.engure.miaosha.controller;

import com.engure.miaosha.service.StockService;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("ms")
@Slf4j
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

    /**
     * 乐观锁 + 令牌桶。使用令牌桶算法，避免同一时刻大量的请求对 mysql 的压力过大
     * @param id
     * @return
     */
    @GetMapping("kill2ken")
    public String killByToken(Integer id) {

        if (!rateLimiter.tryAcquire(2, TimeUnit.SECONDS)) { // 调用服务层业务之前进行限流
            log.info("抢购过于火爆，请重试~~~");
            //throw new RuntimeException("抢购过于火爆，请重试~~~");
            return "为了控制台更好的显示，这里不抛异常，不打印堆栈";
        }

        try {
            int orderId = stockService.kill(id);
            log.info("秒杀成功！，订单编号 " + orderId);
            return "秒杀成功！，订单编号 " + orderId;
        } catch (Exception e) {
            //e.printStackTrace();
            log.info(e.getMessage());
            return e.getMessage();
        }

    }

}
