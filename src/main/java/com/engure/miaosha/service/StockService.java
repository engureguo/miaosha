package com.engure.miaosha.service;


import com.engure.miaosha.entity.Stock;

public interface StockService {

    Stock getStockInfoById(Integer id);

    void updateStock(Stock stock);
}
