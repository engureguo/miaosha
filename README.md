# miaosha
简单的秒杀系统

## 悲观锁思路

踩坑：
```java
@Service
@Transactional
public class StockServiceImpl implements StockService {

    public synchronized int kill(Integer id) throws Exception {

        Stock stock = stockDao.findStockById(id);
        if (stock.getTotal().equals(stock.getSale())) {
            throw new Exception("商品已售空！！");
        } else {
            stock.setSale(stock.getSale() + 1);
            stockDao.updateStockSaleById(stock);
            Order order = new Order();
            order.setSid(stock.getId()).setName(stock.getName()).setCreateTime(new Date());
            orderService.insertOrder(order);
            return order.getId();
        }
        
    }
```
注意!!! `错误说法：业务层加同步代码块`

**悲观锁大坑！多提交的问题：
Transactional和synchronized同时使用初始并发问题。
事务同步范围要比线程同步范围大。
synchronized代码块执行是在事务之内执行的，可以推断在代码块执行完时，事务还未提交，因此其它线程进入synchronized代码块后，读取的数据库数据不是最新的。**

解决方法：
synchronized同步范围大于事务同步范围，在 业务层kill方法之外进行同步，保证释放锁的时候事务已经提交
```java

```





