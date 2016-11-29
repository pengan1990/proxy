package config.model;

/**
 * Created by pengan on 16-5-4.
 */
public class PrivilegeConfig {
    /**
     * DEFAULT_PRIV = false means
     * <p>client has no privilege on insert delete update select operation
     * </p>
     */
    private static final boolean DEFAULT_PRIV = false;
    /**
     * DEFAULT_SHARD_PRIV =true means
     * <p>client have to carry sharding key on insert delete update select operation
     * </p>
     */
    private static final boolean DEFAULT_SHARD_PRIV = true;
    private String userName;
    private boolean insertPriv = DEFAULT_PRIV;
    private boolean deletePriv = DEFAULT_PRIV;
    private boolean updatePriv = DEFAULT_PRIV;
    private boolean selectPriv = DEFAULT_PRIV;
    /**
     * insert have to carry shard key
     */
    private boolean insertWithShardKeyPriv = DEFAULT_SHARD_PRIV;
    private boolean deleteWithShardKeyPriv = DEFAULT_SHARD_PRIV;
    private boolean updateWithShardKeyPriv = DEFAULT_SHARD_PRIV;
    private boolean selectWithShardKeyPriv = DEFAULT_SHARD_PRIV;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setInsertPriv(boolean insertPriv) {
        this.insertPriv = insertPriv;
    }

    public void setDeletePriv(boolean deletePriv) {
        this.deletePriv = deletePriv;
    }

    public void setUpdatePriv(boolean updatePriv) {
        this.updatePriv = updatePriv;
    }

    public void setSelectPriv(boolean selectPriv) {
        this.selectPriv = selectPriv;
    }

    public void setInsertWithShardKeyPriv(boolean insertWithShardKeyPriv) {
        this.insertWithShardKeyPriv = insertWithShardKeyPriv;
    }

    public void setDeleteWithShardKeyPriv(boolean deleteWithShardKeyPriv) {
        this.deleteWithShardKeyPriv = deleteWithShardKeyPriv;
    }

    public void setUpdateWithShardKeyPriv(boolean updateWithShardKeyPriv) {
        this.updateWithShardKeyPriv = updateWithShardKeyPriv;
    }

    public void setSelectWithShardKeyPriv(boolean selectWithShardKeyPriv) {
        this.selectWithShardKeyPriv = selectWithShardKeyPriv;
    }

    public boolean isInsertPriv() {
        return insertPriv;
    }

    public boolean isDeletePriv() {
        return deletePriv;
    }

    public boolean isUpdatePriv() {
        return updatePriv;
    }

    public boolean isSelectPriv() {
        return selectPriv;
    }

    public boolean isInsertWithShardKeyPriv() {
        return insertWithShardKeyPriv;
    }

    public boolean isDeleteWithShardKeyPriv() {
        return deleteWithShardKeyPriv;
    }

    public boolean isUpdateWithShardKeyPriv() {
        return updateWithShardKeyPriv;
    }

    public boolean isSelectWithShardKeyPriv() {
        return selectWithShardKeyPriv;
    }

    public static String getMapKey(String ...combinedKeys) {
        StringBuilder keyBuilder = new StringBuilder();
        for (String key : combinedKeys) {
            keyBuilder.append(key.toUpperCase());
        }
        return keyBuilder.toString();
    }
}
