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

## 接口限流

`限流：是对某一时间窗口内的请求数进行限制，保持系统的可用性和稳定性，防止因流量暴增而导致的系统运行缓慢和宕机`

在面临高并发的抢购请求时，我们如果不对接口进行限流，可能会对后台系统造成极大的压力。大量的请求抢购成功时需要调用下单的接口，过多的请求打到数据库会对系统的稳定性造成影响.

### 接口限流解决方法

常用的限流算法有 `令牌桶` 和 `漏桶(漏斗算法）`，而 Google 开源项目 Guava 中的 RateLimiter 使用的就是令牌桶控制算法。在开发高并发系统时有三把利器用来保护系统：`缓存`、`降级`和`限流`。

- 缓存：缓存的目的是提升系统访问速度和增大系统处理容量
- 降级：降级是当服务器压力剧增的情况下，根据当前业务情况及流量对一些服务和页面有策略的降级，以此释放服务器资源以保证核心任务的正常运行
- 限流：限流的目的是通过对并发访问/请求进行限速，或者对一个时间窗口内的请求进行限速来保护系统，一旦达到限制速率则可以拒绝服务、排队或等待、降级等处理。

### 漏斗算法和令牌桶算法

<img src="images/README.assets/image-20210807014602534.png" alt="image-20210807014602534" style="zoom:80%;" />

- 漏斗算法：漏桶算法思路很简单，水(请求）先进入到漏桶里，漏桶以一定的速度出水，当水流入速度过大会直接溢出，可以看出漏桶算法能强行限制数据的传输速率。
- 令牌桶算法：最初来源于计算机网络。在网络传输数据时，为了防止网络拥塞，需限制流出网络的流量，使流量以比较均匀的速度向外发送。令牌桶算法就实现了这个功能，可控制发送到网络上数据的数目，并允许突发数据的发送。大小固定的令牌桶可自行以恒定的速率源源不断地产生令牌。如果令牌不被消耗，或者被消耗的速度小于产生的速度，令牌就会不断地增多，直到把桶填满。后面再产生的令牌就会从桶中溢出。最后桶中可以保存的最大令牌数永远不会超过桶的大小。这意味，面对瞬时大流量，该算法可以在短时间内请求拿到大量令牌，而且拿令牌的过程并不是消耗很大的事情。



参考：

https://www.cnblogs.com/xuwc/p/9123078.html

http://ifeve.com/guava-ratelimiter/



