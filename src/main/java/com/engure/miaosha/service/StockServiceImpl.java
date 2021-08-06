package com.engure.miaosha.service;

import com.engure.miaosha.dao.StockDao;
import com.engure.miaosha.entity.Order;
import com.engure.miaosha.entity.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class StockServiceImpl implements StockService {

    @Autowired
    private StockDao stockDao;

    @Autowired
    private OrderService orderService;
    public synchronized int kill(Integer id) throws Exception {
        Stock stock = stockDao.findStockById(id);
        // 如果售空，则返回售空，如果没有则返回订单编号
        if (stock.getTotal().equals(stock.getSale())) {
            throw new Exception("商品已售空！！");
        } else {
            stock.setSale(stock.getSale() + 1);
            stockDao.updateStockSaleById(stock);
            // 插入库存表
            Order order = new Order();
            order.setSid(stock.getId()).setName(stock.getName()).setCreateTime(new Date());
            orderService.insertOrder(order);
            return order.getId();
        }
    }

    @Override
    public Stock getStockInfoById(Integer id) {
        return stockDao.findStockById(id);
    }

    @Override
    public void updateStock(Stock stock) {
        stockDao.updateStockSaleById(stock);
    }
}
