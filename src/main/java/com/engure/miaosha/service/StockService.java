package com.engure.miaosha.service;


import com.engure.miaosha.entity.Stock;

public interface StockService {

    int kill(Integer id) throws Exception;

    Stock getStockInfoById(Integer id);

    void updateStock(Stock stock);

    String getMd5(Integer id, Integer uid);

    int killByMd5(Integer id, Integer uid, String md5);
}
