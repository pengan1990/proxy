package conn;

import config.model.InstanceConfig;
import nio.Processor;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by pengan on 16-9-30.
 * <p/>
 * <p/>
 * 连接池设计： 每个processor 一个连接池
 * 写线程 异常 直接关闭连接 不会放回连接池
 * 读线程 异常 直接关闭连接 不会放回连接池
 * <p/>
 * 从连接池当中获取连接：
 * 单线程
 * <p/>
 * 将连接放回连接池
 * 单线程
 * <p/>
 * 连接池的方法： borrow & return & create
 * <p/>
 * 连接存放的地方： 所有的连接都放在队列当中 取的时候从队列取
 * <p/>
 * 连接的要求：
 * 为每个库建立连接池的索引 方便如果需要用到这个库 直接获取 而不需要初始化连接
 * <p/>
 * 连接池需要确保 所有调用获取到的连接是可用的？？？
 */
public class MySQLConnectionPool implements Pool<MySQLConnection> {
    private static final Logger logger = Logger.getLogger(MySQLConnectionPool.class);
    private static final int DEFAULT_NUM = 0;

    private int numActive = DEFAULT_NUM;
    private int numIdle = DEFAULT_NUM;
    private InstanceConfig instance;

    private volatile Map<String, Queue<MySQLConnection>> dbPools;
    private volatile Queue<MySQLConnection> freeChannels;

    public MySQLConnectionPool(InstanceConfig instance) {
        this.instance = instance;
        this.dbPools = new LinkedHashMap<String, Queue<MySQLConnection>>();
        this.freeChannels = new LinkedList<MySQLConnection>();
    }

    /**
     * neither a new MySQLConnection or failure
     * <p/>
     * throw an error
     *
     * @param processor
     * @return
     * @throws IOException
     */
    @Override
    public MySQLConnection create(Processor processor) throws IOException {
        logger.debug("create");
        if (this.numIdle != 0) {
            logger.error("get connection from pool no need to create");
            throw new IOException("get connection from pool no need to create");
        }

        if (this.numIdle + this.numActive > instance.getPoolSize()) {
            logger.warn(this.numIdle + " + " + this.numActive + " > " + instance.getPoolSize());
            throw new IOException(this.numIdle + " + " + this.numActive + " > " + instance.getPoolSize());
        }
        MySQLConnection conn = new MySQLConnection(this, processor);
        this.numActive++; // 需要注意 如果没有创建成功 numActive 不能++ 不能使用try finally 结构
        return conn;
    }

    /**
     * private method for add object to connection pool
     *
     * @param ob
     */
    private void addObject(MySQLConnection ob) {
        logger.debug("addObject");
        if (dbPools.containsKey(ob.getSchema())) {
            dbPools.get(ob.getSchema()).offer(ob);
        } else {
            Queue<MySQLConnection> connQueue = new LinkedList<MySQLConnection>();
            connQueue.offer(ob);
            dbPools.put(ob.getSchema(), connQueue);
        }
        this.freeChannels.offer(ob);
    }

    /**
     * just get the lock then
     *
     * @param schema
     * @return
     * @throws IOException
     */
    @Override
    public MySQLConnection borrowObject(String schema) {
        logger.debug("borrowObject");
        // only idle connection number > 0
        if (this.numIdle > 0) {
            MySQLConnection conn = null;
            Map<String, Queue<MySQLConnection>> pools = dbPools;
            Queue<MySQLConnection> frees = freeChannels;
            if (schema != null && pools.containsKey(schema)) {
                conn = pools.get(schema).poll();
                if (conn != null) {
                    frees.remove(conn);
//                    pools.remove(conn.getSchema());
                    this.numActive++;
                    this.numIdle--;
                    return conn;
                }
            }

            // to here : no idle connection in db pools db may not set
            if (frees.size() > 0) {
                this.numActive++;
                this.numIdle--;
                conn = frees.poll();
                // may be contain in dbPools
                if (conn.getSchema() != null) {
                    pools.get(conn.getSchema()).remove(conn);
                }
                return conn;
            }
        }
        // return null then let client to create
        return null;
    }

    @Override
    public void clear() {
        logger.debug("clear");
        // 正在用的连接就先用着吧
        for (MySQLConnection conn : freeChannels) {
            conn.close();
        }
    }

    @Override
    public int getNumActive() {
        return 0;
    }

    @Override
    public int getNumIdle() {
        return 0;
    }

    /**
     * 所有的连接在放回的时候都会进行重置
     *
     * @param obj
     * @throws IOException
     */
    @Override
    public void returnObject(MySQLConnection obj) {
        logger.debug("returnObject");
        if (this.numIdle + this.numActive < this.instance.getPoolSize()) {
            this.numIdle++;
            addObject(obj);
            this.numActive--;
            return;
        }
        this.numActive--;
        obj.close();
    }

    public int decrementActiveNumAndGet() {
        return --this.numActive;
    }

    public InstanceConfig getInstance() {
        return instance;
    }
}
