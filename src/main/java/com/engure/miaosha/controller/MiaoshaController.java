package com.engure.miaosha.controller;

import com.engure.miaosha.entity.Order;
import com.engure.miaosha.entity.Stock;
import com.engure.miaosha.service.OrderService;
import com.engure.miaosha.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("ms")
public class MiaoshaController {

    @Autowired
    private StockService stockService;

    @Autowired
    private OrderService orderService;

    @GetMapping("kill")
    public String kill(Integer id) {

        // 查看商品是否售空
        Stock stock = stockService.getStockInfoById(id);
        // 如果售空，则返回售空，如果没有则返回订单编号
        if (stock.getTotal().equals(stock.getSale())) {
            return "商品已售空！！！";
        } else {
            stock.setSale(stock.getSale() + 1);
            stockService.updateStock(stock);
            // 插入库存表
            Order order = new Order();
            order.setSid(stock.getId()).setName(stock.getName()).setCreateTime(new Date());
            orderService.insertOrder(order);
            return "订单创建成功 " + order.getId();
        }
    }

}
