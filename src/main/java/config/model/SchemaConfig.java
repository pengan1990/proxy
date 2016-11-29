package config.model;


import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SchemaConfig {

    private final String id;
    private final String name;
    private final boolean noSharding;
    private final String[] metaDataNodes;
    private final Set<String> allDataNodes;
    private final Map<String, TableConfig> tables;

    //use for public cloud shard_database
    private volatile int maxCapacity;
    private volatile int curCapacity;
    private volatile int maxConns;
    private volatile AtomicInteger curConns;

    //use for jtransfer
    private volatile Timestamp lockedTime;
    private volatile AtomicBoolean locked;

    public SchemaConfig(String id, String name, Map<String, TableConfig> tables, boolean noSharding) {

        this.id = id;
        this.name = name;
        this.tables = tables;
        this.noSharding = noSharding;
        this.metaDataNodes = buildMetaDataNodes();
        this.allDataNodes = buildAllDataNodes();
        this.locked = new AtomicBoolean(false);
        this.curConns = getPreviousCurrConn();
    }

    public String getName() {
        return name;
    }

    public String getNoShardingNode() {
        if (noSharding) {
            return metaDataNodes[0];
        }
        return null;
    }

    public Map<String, TableConfig> getTables() {
        return tables;
    }

    public boolean isNoSharding() {
        return noSharding;
    }

    public String[] getMetaDataNodes() {
        return metaDataNodes;
    }

    public Set<String> getAllDataNodes() {
        return allDataNodes;
    }

    public String getRandomDataNode() {
        if (allDataNodes == null || allDataNodes.isEmpty()) {
            return null;
        }
        return allDataNodes.iterator().next();
    }

    /**
     * 取得含有不同Meta信息的数据节点,比如表和表结构。
     */
    private String[] buildMetaDataNodes() {
        Set<String> set = new HashSet<String>();

        if (tables == null || tables.containsKey(TableConfig.PLACE_HOLDER_TABLE)) {
            set.add(tables.get(TableConfig.PLACE_HOLDER_TABLE).getDataNodes()[0]);
        }

        if (!noSharding) {
            for (TableConfig tc : tables.values()) {
                set.add(tc.getDataNodes()[0]);
            }
        }
        return set.toArray(new String[set.size()]);
    }

    /**
     * 取得该schema的所有数据节点
     */
    private Set<String> buildAllDataNodes() {
        Set<String> set = new HashSet<String>();
        if (tables == null || tables.containsKey(TableConfig.PLACE_HOLDER_TABLE)) {
            set.add(tables.get(TableConfig.PLACE_HOLDER_TABLE).getDataNodes()[0]);
        }

        if (!noSharding) {
            for (TableConfig tc : tables.values()) {
                set.addAll(Arrays.asList(tc.getDataNodes()));
            }
        }
        return set;
    }

    public String getId() {
        return id;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getCurCapacity() {
        return curCapacity;
    }

    public void setCurCapacity(int curCapacity) {
        this.curCapacity = curCapacity;
    }

    public int getMaxConns() {
        return maxConns;
    }

    public void setMaxConns(int maxConns) {
        this.maxConns = maxConns;
    }

    public boolean getLocked() {
        return this.locked.get();
    }

    public void setLocked(boolean locked) {
        this.locked.set(locked);
    }

    public AtomicInteger getCurConns() {
        return curConns;
    }

    public void setCurConns(AtomicInteger curConns) {
        this.curConns = curConns;
    }

    public Timestamp getLockedTime() {
        return lockedTime;
    }

    public void setLockedTime(Timestamp lockedTime) {
        this.lockedTime = lockedTime;
    }

    public void setLocked(AtomicBoolean locked) {
        this.locked = locked;
    }

    /**
     * get the previous currConns if not exist then make new default 0
     *
     * @return AtomicInteger
     */
    private AtomicInteger getPreviousCurrConn() {
        return new AtomicInteger();
    }

    public String toString() {
        return new StringBuilder().append("SchemaConfig [")
                .append("id:").append(id)
                .append(", name:").append(name)
                .append(", noSharding:").append(noSharding)
                .append(", maxCapacity:").append(maxCapacity)
                .append(", curCapacity:").append(curCapacity)
                .append(", maxConns:").append(maxConns)
                .append(", curConns:").append(curConns.get())
                .append(", lockedTime:").append(lockedTime)
                .append(", locked:").append(locked)
                .append("]").toString();
    }
}