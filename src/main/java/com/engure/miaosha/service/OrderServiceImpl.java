package com.engure.miaosha.service;

import com.engure.miaosha.dao.OrderDao;
import com.engure.miaosha.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderDao orderDao;

    @Override
    public void insertOrder(Order order) {
        orderDao.insertOrder(order);
    }
}
