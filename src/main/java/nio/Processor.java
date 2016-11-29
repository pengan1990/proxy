package nio;

import buffer.BufferPool;
import config.loader.MetaConfig;
import config.model.InstanceConfig;
import conn.AbstractConnection;
import conn.MySQLConnection;
import conn.MySQLConnectionPool;
import conn.ServerConnection;
import org.apache.log4j.Logger;
import server.ProxyServer;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by pengan on 16-9-30.
 */
public class Processor {
    private static final Logger logger = Logger.getLogger(Processor.class);
    private static final int CHUNK_SIZE = 4096;
    private static final int BUFFER_SIZE = 32;

    private RWReactor reactor;
    private Map<Integer, ServerConnection> frontends;

    //<instanceId, MySQLConnectionPool>
    private Map<String, MySQLConnectionPool> instancePools;
    protected BufferPool bufferPool;

    public Processor(RWReactor reactor) {
        this.reactor = reactor;
        this.frontends = new LinkedHashMap<Integer, ServerConnection>();
        this.bufferPool = new BufferPool(BUFFER_SIZE, CHUNK_SIZE);
        this.instancePools = new LinkedHashMap<String, MySQLConnectionPool>();
    }

    /**
     * front end register there is an thread swap
     *
     * @param connection
     */
    public void frontendRegister(AbstractConnection connection) {
        connection.setProcessor(this);
        this.reactor.register(connection);
    }

    /**
     * register on select directly
     *
     * @param conn
     * @throws ClosedChannelException
     */
    public void backendRegister(MySQLConnection conn) throws IOException {
        conn.setProcessor(this);
        this.reactor.backendRegister(conn);
    }

    public void start() {
        logger.debug("start");
        initPool();
        this.reactor.start();
    }

    /**
     * one processor one pool
     */
    private void initPool() {
        logger.debug("initPool");
        MetaConfig metaConfig = ProxyServer.getINSTANCE().getMetaConfig();
        for (Map.Entry<String, InstanceConfig> entry : metaConfig.getInstances().entrySet()) {
            instancePools.put(entry.getKey(), new MySQLConnectionPool(entry.getValue()));
        }
    }


    public RWReactor getReactor() {
        return reactor;
    }

    public Map<Integer, ServerConnection> getFrontends() {
        return frontends;
    }

    public BufferPool getBufferPool() {
        return bufferPool;
    }

    public Map<String, MySQLConnectionPool> getInstancePools() {
        return instancePools;
    }
}
