package com.engure.miaosha.service;

import com.engure.miaosha.dao.StockDao;
import com.engure.miaosha.entity.Order;
import com.engure.miaosha.entity.Stock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@Slf4j
public class StockServiceImpl implements StockService {

    @Autowired
    private StockDao stockDao;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public int kill(Integer id) {
        Stock stock = stockDao.findStockById(id);
        // 如果售空，则返回售空，如果没有则返回订单编号
        if (stock.getTotal().equals(stock.getSale())) {
            throw new RuntimeException("商品已售空！！");
        } else {
            Integer res = stockDao.updateStockAndVersionSaleById(stock); // 同时更新 sale 和 version 两个字段
            if (res == 0) {
                throw new RuntimeException("秒杀失败！！");
            }
            // 插入库存表
            Order order = new Order();
            order.setSid(stock.getId()).setName(stock.getName()).setCreateTime(new Date());
            orderService.insertOrder(order);
            return order.getId();
        }
    }

    @Override
    public Stock getStockInfoById(Integer id) {
        return stockDao.findStockById(id);
    }

    @Override
    public void updateStock(Stock stock) {
        stockDao.updateStockSaleById(stock);
    }

    /**
     * 根据商品id和用户uid获取验证值
     */
    @Override
    public String getMd5(Integer id, Integer uid) {

        if (id == null || uid == null)
            throw new RuntimeException("商品id或用户id不合法！！");

        // 验证商品id合法性（略）——> 查询数据库
        // 验证用户id合法性（略）——> 查询数据库

        String key = "MS_KEY_" + id + "_" + uid;// MS_KEY_商品id_用户id
        String salt = "!!!Q*?...#";
        int liveTime = 3600;//超时时间，为了测试 但用户访问 频率，这个值调大
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            String from = System.currentTimeMillis() + salt;
            value = DigestUtils.md5DigestAsHex(from.getBytes(StandardCharsets.UTF_8));//时间戳 + salt
        }
        stringRedisTemplate.opsForValue().set(key, value, liveTime, TimeUnit.SECONDS);//刷新验证值超时时间
        log.info("用户验证值获取：用户{}，商品{}, md5{}", uid, id, value);
        return value;
    }

    /**
     * 用户通过验证值md5秒杀
     */
    @Override
    public int killByMd5(Integer id, Integer uid, String md5) {

        if (id == null || uid == null || md5 == null)
            throw new RuntimeException("参数不合法，请重试~~~");

        String key = "MS_KEY_" + id + "_" + uid;
        String value = stringRedisTemplate.opsForValue().get(key);
        log.info("验证用户：key={}, value={}", key, value);
        if (value == null || !value.equals(md5))
            throw new RuntimeException("请求数据不合法，请重试~~");

        return kill(id);
    }

    /**
     * 是否允许访问：检查 md5 、检查访问频率
     */
    @Override
    public boolean allowVisit(Integer id, Integer uid, String md5) {

        if (id == null || uid == null || md5 == null)
            throw new RuntimeException("参数不合法，请重试~~~");

        // 检查md5(功能与 killByMd5方法重复)
        String key = "MS_KEY_" + id + "_" + uid;
        String value = stringRedisTemplate.opsForValue().get(key);
        log.info("验证用户：key={}, value={}", key, value);
        if (value == null || !value.equals(md5))
            throw new RuntimeException("验证信息不合法，请重试~~");

        // 检查访问频次
        String freKey = "LIMIT_VISIT_" + id + "_" + uid;
        // duration内最多有maxTimes次访问
        int maxTimes = 10;
        int duration = 3;
        String freValue = stringRedisTemplate.opsForValue().get(freKey);
        log.info("用户访问：key={}, value={}", freKey, freValue);

        if (freValue == null) // 用户没有访问或者上一轮访问限制到时
            stringRedisTemplate.opsForValue().set(freKey, "1", duration, TimeUnit.SECONDS);
        else if (freValue.equals(String.valueOf(maxTimes)))
            throw new RuntimeException("当前活动较为火爆，请重试~~~（访问次数过多）");//达到次数后，需要限制访问，直到 LIMIT_1_1 超时
        else {
            String newV = String.valueOf(Integer.parseInt(freValue) + 1);
            stringRedisTemplate.opsForValue().set(freKey, newV, duration, TimeUnit.SECONDS);//更新时间
        }
        return true;
    }


}







