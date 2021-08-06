package com.engure.miaosha.dao;

import com.engure.miaosha.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockDao {
    Stock findStockById(Integer id);

    void updateStockSaleById(Stock stock);
}
