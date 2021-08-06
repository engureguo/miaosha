package com.engure.miaosha.dao;

import com.engure.miaosha.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockDao {
    Stock findStockById(Integer id);

    void updateStockSaleById(Stock stock);

    // 返回更新条数，判断是否秒杀成功
    Integer updateStockAndVersionSaleById(Stock stock);
}
