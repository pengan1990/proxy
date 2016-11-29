package config.model;

/**
 * 描述一个数据源的配置
 */
public final class InstanceConfig {

    private static final int DEFAULT_SQL_RECORD_COUNT = 10;
    private static final int DEFAULT_POOL_SIZE = 256;
    private static final int DEFAULT_INIT_POOL_SIZE = 10;
    private static final long DEFAULT_WAIT_TIMEOUT = 5 * 1000L;
    private static final long DEFAULT_IDLE_TIMEOUT = 1800 * 1000L;  //后端链接30分钟超时
    private static final int DEFAULT_POOL_ELASTIC_SIZE = 30;

    private final String id;
    private String name;
    private String host;
    private int port;
    private String user;
    private String password;
    private String sqlMode;
    private String clusterId;
    private String instanceId;
    private int sqlRecordCount = DEFAULT_SQL_RECORD_COUNT;
    private int initSize = DEFAULT_INIT_POOL_SIZE;
    private int poolSize = DEFAULT_POOL_SIZE;// 保持后端数据通道的默认最大值
    private long waitTimeout = DEFAULT_WAIT_TIMEOUT; // 取得新连接的等待超时时间
    private long idleTimeout = DEFAULT_IDLE_TIMEOUT; // 连接池中连接空闲超时时间
    private int poolElasticSize = DEFAULT_POOL_ELASTIC_SIZE;


    public InstanceConfig(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getSqlMode() {
        return sqlMode;
    }

    public void setSqlMode(String sqlMode) {
        this.sqlMode = sqlMode;
    }

    public int getSqlRecordCount() {
        return sqlRecordCount;
    }

    public void setSqlRecordCount(int sqlRecordCount) {
        this.sqlRecordCount = sqlRecordCount;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public long getWaitTimeout() {
        return waitTimeout;
    }

    public void setWaitTimeout(long waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public int getInitSize() {
        return initSize;
    }

    public void setInitSize(int initSize) {
        this.initSize = initSize;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[")
                .append("name = ").append(name)
                .append(" , host = ").append(host)
                .append(" , port = ").append(port)
                .append(" , user = ").append(user)
//        		.append(" , password = ").append(password)
                .append(" , poolSize = ").append(poolSize)
                .append(" , waitTimeout = ").append(waitTimeout)
                .append(" , idleTimeout = ").append(idleTimeout)
                .append(']').toString();
    }

    public int getPoolElasticSize() {
        return poolElasticSize;
    }

    public void setPoolElasticSize(int poolElasticSize) {
        this.poolElasticSize = poolElasticSize;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InstanceConfig) {
            InstanceConfig instance = (InstanceConfig) obj;
            return this.host.equals(instance.host) && this.port == instance.port;
        }
        return false;
    }
}
