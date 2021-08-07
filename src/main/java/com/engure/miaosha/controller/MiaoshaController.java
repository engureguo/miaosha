package com.engure.miaosha.controller;

import com.engure.miaosha.service.StockService;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;//操作redis

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

    /**
     * 乐观锁防超卖 + 令牌桶限流 + 限制抢购时间
     */
    @GetMapping("kill3")
    public String killByTokenByExpire(Integer id) {

        if (!stringRedisTemplate.hasKey("kill" + id)) { // 规定缓存中超时记录的键为 <kill + 商品id>
            //throw new RuntimeException("抢购已结束~~~");
            log.info("抢购已结束!!~");
            return "over";
        }

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

    /*****************************/

    @GetMapping("getmd5")
    public String getMD5(Integer id, Integer uid) {

        try {
            String md5 = stockService.getMd5(id, uid);
            return "获取到验证值为 " + md5;
        } catch (Exception e) {
            //e.printStackTrace();
            return e.getMessage();
        }

    }

    /**
     * 乐观锁防超卖 + 令牌桶限流 + md5签名（隐藏 getMd5 接口！）
     */
    @GetMapping("killbymd5")
    public String killByMd5(Integer id, Integer uid, String md5) {

        // 这里主要为了测试<接口隐藏>功能，不考虑超时抢购
        //if (!stringRedisTemplate.hasKey("kill" + id)) { // 规定缓存中超时记录的键为 <kill + 商品id>
        //    //throw new RuntimeException("抢购已结束~~~");
        //    log.info("抢购已结束!!~");
        //    return "over";
        //}

        if (!rateLimiter.tryAcquire(2, TimeUnit.SECONDS)) { // 调用服务层业务之前进行限流
            log.info("抢购过于火爆，请重试~~~");
            //throw new RuntimeException("抢购过于火爆，请重试~~~");
            return "为了控制台更好的显示，这里不抛异常，不打印堆栈";
        }

        try {
            int orderId = stockService.killByMd5(id, uid, md5);
            log.info("秒杀成功！，订单编号 " + orderId);
            return "秒杀成功！，订单编号 " + orderId;
        } catch (Exception e) {
            //e.printStackTrace();
            log.info(e.getMessage());
            return e.getMessage();
        }

    }

    /**
     * 乐观锁防超卖 + 令牌桶限流 + md5签名（隐藏 getMd5 接口！） + 单用户访问频率限制
     */
    @GetMapping("killtms")
    public String killByMd5AndTimes(Integer id, Integer uid, String md5) {

        // 这里主要为了测试<限制访问频率>的功能，不考虑超时抢购，需要考虑md5
        //if (!stringRedisTemplate.hasKey("kill" + id)) { // 规定缓存中超时记录的键为 <kill + 商品id>
        //    //throw new RuntimeException("抢购已结束~~~");
        //    log.info("抢购已结束!!~");
        //    return "over";
        //}

        try {
            stockService.allowVisit(id, uid, md5);//需要验证值md5且不超时，检查访问频率
        } catch (Exception e) {
            //e.printStackTrace();
            log.info(e.getMessage());
            return e.getMessage();
        }

        if (!rateLimiter.tryAcquire(2, TimeUnit.SECONDS)) { // 调用服务层业务之前进行限流
            log.info("抢购过于火爆，请重试~~~");
            //throw new RuntimeException("抢购过于火爆，请重试~~~");
            return "为了控制台更好的显示，这里不抛异常，不打印堆栈";
        }

        try {
            int orderId = stockService.kill(id);//上边已经检验过md5
            log.info("秒杀成功！，订单编号 " + orderId);
            return "秒杀成功！，订单编号 " + orderId;
        } catch (Exception e) {
            //e.printStackTrace();
            log.info(e.getMessage());
            return e.getMessage();
        }

    }


}
