package com.engure.miaosha.service;


import com.engure.miaosha.entity.Stock;

public interface StockService {

    int kill(Integer id) throws Exception;

    Stock getStockInfoById(Integer id);

    void updateStock(Stock stock);
}
