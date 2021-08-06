package com.engure.miaosha.controller;

import com.engure.miaosha.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ms")
public class MiaoshaController {

    @Autowired
    private StockService stockService;

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
