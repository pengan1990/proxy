package config;

import config.model.DataNodeConfig;
import conn.MySQLConnectionPool;

/**
 * Created by pengan on 16-10-6.
 */
public final class MySQLDataNode {
    //对应数据库datanode_info
    private String id;
    private final String name;
    private String database;//后端mysql实例上的真实database的名字，f.g. dbtest1
    private DataNodeConfig config;
    private volatile MySQLConnectionPool connectionPool;
    private long executeCount;


    public MySQLDataNode(DataNodeConfig dnConfig) {
        this.config = dnConfig;
        this.id = dnConfig.getId();
        this.name = dnConfig.getName();
        this.database = dnConfig.getDatabase();
    }

    public String getName() {
        return name;
    }

    public DataNodeConfig getConfig() {
        return config;
    }

    public long getExecuteCount() {
        return executeCount;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MySQLConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public void setMySQLConnectionPool(MySQLConnectionPool dataSource) {
        this.connectionPool = dataSource;
    }

    public String toString(){

        return new StringBuilder().append("DATANODE:[")
                .append("name=").append(name)
                .append(", database").append(database)
                .append("DATASOURCE:").append(connectionPool)
                .append(" ]").toString();
    }
}
