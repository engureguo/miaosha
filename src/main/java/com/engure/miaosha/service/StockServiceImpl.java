package com.engure.miaosha.service;

import com.engure.miaosha.dao.StockDao;
import com.engure.miaosha.entity.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StockServiceImpl implements StockService{

    @Autowired
    private StockDao stockDao;

    @Override
    public Stock getStockInfoById(Integer id) {
        return stockDao.findStockById(id);
    }

    @Override
    public void updateStock(Stock stock) {
        stockDao.updateStockSaleById(stock);
    }
}
