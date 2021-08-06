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
@RestController
@RequestMapping("ms")
public class MiaoshaController {
    
    @GetMapping("kill")
    public String kill(Integer id) {

        try {
            synchronized (this) {   // 控制层的调用处加锁
                int orderId = stockService.kill(id);
                return "秒杀成功！，订单编号 " + orderId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }

    }
}
```

可以解决问题（单机下）
缺点：一个线程拿到锁其他线程处于阻塞状态，用户体验差，服务器压力大，吞吐量小


## 乐观锁（利用数据库锁机制

数据库层面上过滤到一些请求

实际上是把防止超卖问题交给数据库解决，利用 **表中的version字段和数据库的事务** 避免超卖问题

使用表中的version字段：
1. `select id,num,version from stock where id = 1`
2. `update stock set sale=sale+1, version=version+1 where id=1 and version=#{version}`











