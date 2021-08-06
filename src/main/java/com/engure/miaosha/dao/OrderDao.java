package com.engure.miaosha.dao;

import com.engure.miaosha.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderDao {
    void insertOrder(Order order);
}
