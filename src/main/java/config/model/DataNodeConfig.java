package config.model;

/**
 * 用于描述一个数据节点的配置
 */
public final class DataNodeConfig {

    private String id;
    private String name;
    private String database;
    private String instanceId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
}