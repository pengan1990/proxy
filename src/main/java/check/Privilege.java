package check;


import config.loader.MetaConfig;
import server.ProxyServer;

/**
 * Created by pengan on 16-9-29.
 */
public class Privilege {
    private static MetaConfig META_CONFIG = ProxyServer.getINSTANCE().getMetaConfig();

    public static boolean userExist(String user) {
        return META_CONFIG.getUsers().containsKey(user);
    }

    public static String getPassword(String user) {
        return META_CONFIG.getUsers().get(user).getPassword();
    }

    public static boolean schemaExist(String user, String schema) {
        return META_CONFIG.getUsers().get(user).getSchemas().contains(schema.toLowerCase());
    }

    public static boolean tableExist(String schema, String table) {
        return META_CONFIG.getSchemaTables().get(schema.toLowerCase()).containsKey(table.toLowerCase());
    }

    public static MetaConfig getMetaConfig() {
        return META_CONFIG;
    }
}
