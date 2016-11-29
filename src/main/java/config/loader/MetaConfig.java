package config.loader;

import config.MySQLDataNode;
import config.model.*;
import config.model.rule.RuleAlgorithm;
import config.model.rule.RuleConfig;
import conn.MySQLConnectionPool;
import exception.ConfigException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import parse.ast.expression.primary.PlaceHolder;
import route.PartitionByString;
import util.JsonUtil;
import util.SplitUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MetaConfig {
    // 规定 所有的表名和库名都用大写
    private final Logger logger = Logger.getLogger(MetaConfig.class);

    private final ServerConfig config;
    private JsonNode rootNode;
    private final String schemaJson;
    private volatile Map<String, RuleConfig> rules;
    private volatile Map<String, RuleAlgorithm> ruleFuncs;
    private volatile Map<String, InstanceConfig> instances;
    private volatile Map<String, DataNodeConfig> dataNodes;
    private volatile Map<String, Map<String, TableConfig>> schemaTables;
    private volatile Map<String, MySQLConnectionPool> connectionPools;
    private volatile Map<String, MySQLDataNode> mySQLDataNodes;
    private volatile Map<String, SchemaConfig> schemas;
    private volatile Map<String, UserConfig> users;
    private volatile Map<String, SpecialRouteConfig> specialRoute;
    private volatile Map<String, PrivilegeConfig> privileges;

    private volatile Map<String, RuleConfig> _rules;
    private volatile Map<String, RuleAlgorithm> _ruleFuncs;
    private volatile Map<String, InstanceConfig> _instances;
    private volatile Map<String, DataNodeConfig> _dataNodes;
    private volatile Map<String, Map<String, TableConfig>> _schemaTables;
    private volatile Map<String, MySQLConnectionPool> _connectionPools;
    private volatile Map<String, MySQLDataNode> _mySQLDataNodes;
    private volatile Map<String, SchemaConfig> _schemas;
    private volatile Map<String, UserConfig> _users;
    private volatile Map<String, SpecialRouteConfig> _specialRoute;
    private volatile Map<String, PrivilegeConfig> _privileges;

    public MetaConfig(ServerConfig config) throws ConfigException {
        this.config = config;
        this.schemaJson = requestSchemaJson(config);
        load(config);
        this.connectionPools = initInstancePools();
        this.mySQLDataNodes = initDataNodes();
    }

    private void reset() {
        rootNode = null;
        rules = null;
        ruleFuncs = null;
        instances = null;
        dataNodes = null;
        schemaTables = null;
        connectionPools = null;
        mySQLDataNodes = null;
        schemas = null;
        users = null;
        specialRoute = null;
        privileges = null;

        _rules = null;
        _ruleFuncs = null;
        _instances = null;
        _dataNodes = null;
        _schemaTables = null;
        _connectionPools = null;
        _mySQLDataNodes = null;
        _schemas = null;
        _users = null;
        _specialRoute = null;
        _privileges = null;
    }

    private Map<String, MySQLConnectionPool> initInstancePools() {
        Map<String, InstanceConfig> dscs = getInstances();
        Map<String, MySQLConnectionPool> MySQLConnectionPools = new HashMap<String, MySQLConnectionPool>();
        MySQLConnectionPool mds = null;
        for (InstanceConfig dsc : dscs.values()) {
            mds = new MySQLConnectionPool(dsc);
            MySQLConnectionPools.put(dsc.getId(), mds);
        }
        return MySQLConnectionPools;
    }

    private Map<String, MySQLDataNode> initDataNodes() throws ConfigException {

        Map<String, DataNodeConfig> nodeConfs = getDataNodes();
        Map<String, MySQLDataNode> nodes = new HashMap<String, MySQLDataNode>(nodeConfs.size());
        for (DataNodeConfig conf : nodeConfs.values()) {
            MySQLDataNode dataNode = getDataNode(conf);
            if (nodes.containsKey(dataNode.getName())) {
                throw new ConfigException("dataNode " + dataNode.getName() + " duplicated!");
            }
            nodes.put(dataNode.getId(), dataNode);
        }
        return nodes;
    }

    private MySQLDataNode getDataNode(DataNodeConfig dnc) {

        MySQLDataNode node = new MySQLDataNode(dnc);
        node.setMySQLConnectionPool(connectionPools.get(dnc.getInstanceId()));
        return node;
    }


    private String requestSchemaJson(ServerConfig loader) throws ConfigException {

        final String URL = loader.getManagerUrl();
        logger.debug("request jmanager url :" + URL);
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(URL);
        String result;

        int statusCode = 0;
        try {
            statusCode = httpClient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            result = getMethod.getResponseBodyAsString();
            logger.debug(result);
        } catch (Exception e) {
            throw new ConfigException("request schemaJson from jmanager error !!" + e.getMessage());
        } finally {
            getMethod.releaseConnection();
        }

        return result;
    }

    private void load(ServerConfig ServerLoader) throws ConfigException {

        try {

            this.rootNode = JsonUtil.parseJson(schemaJson);
            loadFuncs();
            loadRules();
            loadSpecialRoute();
            loadInstances(ServerLoader);
            loadDataNodes();
            loadTables();
            loadSchemas();
            loadTransferSchemas();
            loadUsers();
            loadPrivileges();
            loadManager();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("load json info from jmanager error ." + e.getMessage());
            throw new ConfigException("load json info from jmanager error .", e);
        }
    }

    private void loadFuncs() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InvocationTargetException, ConfigException {
        ruleFuncs = new HashMap<String, RuleAlgorithm>();


        JsonNode ruleFuncNode = rootNode.findValue("ruleFuncInfos");
        if (ruleFuncNode == null) {
            logger.warn("ruleFuncNode is null");
            return;
        }

        String id, className, partitionWeight1;
        int partitionCount1;
        Iterator<JsonNode> it = ruleFuncNode.iterator();
        while (it.hasNext()) {
            JsonNode jsonNode = it.next();
            id = jsonNode.findValue("id").getTextValue();
            className = "route.PartitionByString";
            partitionCount1 = jsonNode.findValue("partitionCount1").asInt();
            partitionWeight1 = jsonNode.findValue("partitionWeight1").getTextValue();
            RuleAlgorithm function = createFunction(id, className);
            function = function.constructMe(String.valueOf(partitionCount1), partitionWeight1);

            if (ruleFuncs.containsKey(id)) {
                throw new ConfigException("ruleFuncs id " + id + " duplicated!");
            }

            ruleFuncs.put(id, function);
        }
    }

    private RuleAlgorithm createFunction(String name, String clazz)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InvocationTargetException, ConfigException {
        Class<?> clz = Class.forName(clazz);
        if (!RuleAlgorithm.class.isAssignableFrom(clz)) {
            throw new IllegalArgumentException("rule function must implements "
                    + RuleAlgorithm.class.getName() + ", name=" + name);
        }
        Constructor<?> constructor = null;
        for (Constructor<?> cons : clz.getConstructors()) {
            Class<?>[] paraClzs = cons.getParameterTypes();
            if (paraClzs != null && paraClzs.length == 0) {
                constructor = cons;
                break;
            }
        }

        if (constructor == null) {
            throw new ConfigException(
                    "function "
                            + name
                            + " with class of "
                            + clazz
                            + " must have a constructor with one parameter");
        }

        return (RuleAlgorithm) constructor.newInstance();
    }

    private void loadRules() throws ConfigException {

        rules = new HashMap<String, RuleConfig>();

        JsonNode ruleNode = rootNode.findValue("ruleInfos");
        if (ruleNode == null) {
            logger.warn("ruleNode is null");
            return;
        }
        String id, columns, functionId;
        Iterator<JsonNode> it = ruleNode.iterator();
        RuleConfig rule = null;

        while (it.hasNext()) {

            JsonNode jsonNode = it.next();
            id = jsonNode.findValue("id").getTextValue();
            functionId = jsonNode.findValue("functionId").getTextValue();
            columns = jsonNode.findValue("columns").getTextValue().trim();
            RuleAlgorithm funcExpr = ruleFuncs.get(functionId);

            String[] columnArray = null;
            columnArray = SplitUtil.split(columns, ',', true);
            if (funcExpr instanceof PartitionByString) {
                PlaceHolder placeHolder = new PlaceHolder(columns, columns.toUpperCase());
                placeHolder.setCacheEvalRst(false);
            } else {
                for (int i = 0; i < columnArray.length; ++i) {
                    String columnUpper = columnArray[i].toUpperCase();
                    PlaceHolder placeHolder = new PlaceHolder(columnArray[i], columnUpper);
                    placeHolder.setCacheEvalRst(false);
                    columnArray[i] = columnUpper;
                }
            }
            funcExpr.initialize();
            rule = new RuleConfig(id, columnArray, funcExpr);

            if (rules.containsKey(id)) {
                throw new ConfigException("rules id " + rule.getId() + " duplicated!");
            }

            rules.put(id, rule);
        }
    }

    private void loadInstances(ServerConfig ServerLoader) throws ConfigException {

        instances = new HashMap<String, InstanceConfig>();
        JsonNode dataSourceNode = rootNode.findValue("instanceInfos");

        if (dataSourceNode == null) {
            logger.error("tableRuleNode is null");
            return;
        }

        String id, name, host, user, password, sqlMode;
        int initSize, poolSize, port;
        long waitTimeout, idleTimeout;
        InstanceConfig instanceConfig = null;
        Iterator<JsonNode> it = dataSourceNode.iterator();
        while (it.hasNext()) {
            JsonNode jsonNode = it.next();
            id = jsonNode.findValue("id").getTextValue();
            name = jsonNode.findValue("name").getTextValue();
            host = jsonNode.findValue("ip").getTextValue();
            port = jsonNode.findValue("port").asInt();
            user = jsonNode.findValue("user").getTextValue();
            password = jsonNode.findValue("password").getTextValue();
            sqlMode = jsonNode.findValue("sqlMode").getTextValue();
            initSize = jsonNode.findValue("initSize").asInt();
            poolSize = jsonNode.findValue("poolSize").asInt();
            waitTimeout = jsonNode.findValue("waitTimeout").asLong();
            idleTimeout = jsonNode.findValue("idleTimeout").asLong();

            instanceConfig = new InstanceConfig(id, name);
            instanceConfig.setHost(host);
            instanceConfig.setPort(port);
            instanceConfig.setUser(user);
            instanceConfig.setPassword(password);
            instanceConfig.setSqlMode(sqlMode);
            instanceConfig.setInitSize(initSize);
            instanceConfig.setPoolSize(poolSize);
            instanceConfig.setWaitTimeout(waitTimeout);
            instanceConfig.setIdleTimeout(idleTimeout);

            instanceConfig.setPoolElasticSize(100);


            if (instances.containsKey(id)) {
                throw new ConfigException("instance id " + instanceConfig.getId() + " duplicated!");
            }
            instances.put(id, instanceConfig);
        }
    }

    private void loadDataNodes() throws ConfigException {

        dataNodes = new HashMap<String, DataNodeConfig>();
        JsonNode dataNode = rootNode.findValue("dataNodeInfos");

        if (dataNode == null) {
            logger.warn("dataNode is null");
            return;
        }

        String id, name, instanceId;
        DataNodeConfig dataNodeConfig = null;
        Iterator<JsonNode> it = dataNode.iterator();
        while (it.hasNext()) {
            JsonNode jsonNode = it.next();
            id = jsonNode.findValue("id").getTextValue().trim();
            name = jsonNode.findValue("name").getTextValue().trim();
            instanceId = jsonNode.findValue("instanceId").getTextValue().trim();

            dataNodeConfig = new DataNodeConfig();
            dataNodeConfig.setId(id);
            dataNodeConfig.setName(name);
            dataNodeConfig.setDatabase(name);
            dataNodeConfig.setInstanceId(instanceId);


            if (dataNodes.containsKey(id)) {
                throw new ConfigException("dataNode id " + dataNodeConfig.getId() + " duplicated!");
            }

            dataNodes.put(id, dataNodeConfig);
        }
    }

    private void loadSpecialRoute() {

        specialRoute = new HashMap<String, SpecialRouteConfig>();
        JsonNode spRoute = rootNode.findValue("specialRouteInfos");

        if (spRoute == null) {
            logger.warn("no special route!");
            return;
        }

        String id, tableId, column, columnValue, datanodeId;
        SpecialRouteConfig specialRouteConfig = null;
        Iterator<JsonNode> it = spRoute.iterator();
        while (it.hasNext()) {
            JsonNode jsonNode = it.next();
            id = jsonNode.findValue("id").getTextValue();
            tableId = jsonNode.findValue("tableId").getTextValue();
            datanodeId = jsonNode.findValue("datanodeId").getTextValue();
            column = jsonNode.findValue("column").getTextValue();
            columnValue = jsonNode.findValue("columnValue").getTextValue();

            specialRouteConfig = new SpecialRouteConfig(id, tableId, column, columnValue, datanodeId);
            specialRoute.put(id, specialRouteConfig);
        }
    }


    private void loadTables() {

        schemaTables = new HashMap<String, Map<String, TableConfig>>();
        JsonNode tableNode = rootNode.findValue("tableInfos");

        if (tableNode == null) {
            logger.error("tableNode is null");
            return;
        }

        String id, name, schemaId;
        JsonNode dataNodeJsonNode = null;
        JsonNode ruleJsonNode = null;
        TableConfig tableConfig = null;
        Map<String, TableConfig> tables = null;
        List<RuleConfig> rules = null;

        Iterator<JsonNode> it = tableNode.iterator();
        while (it.hasNext()) {

            JsonNode jsonNode = it.next();
            id = jsonNode.findValue("id").getTextValue();
            name = jsonNode.findValue("name").getTextValue();
            schemaId = jsonNode.findValue("schemaId").getTextValue();
            ruleJsonNode = jsonNode.findValue("ruleIds");
            dataNodeJsonNode = jsonNode.findValue("dataNodeIds");
            Map<String, Map<String, String>> colValNode = new HashMap<String, Map<String, String>>();
            String[] dataNodeArray = new String[dataNodeJsonNode.size()];
            for (int index = 0; index < dataNodeJsonNode.size(); index++) {
                JsonNode node = dataNodeJsonNode.get(index);
                dataNodeArray[index] = node.getTextValue();
            }

            rules = new ArrayList<RuleConfig>();
            for (int index = 0; index < ruleJsonNode.size(); index++) {
                JsonNode node = ruleJsonNode.get(index);
                rules.add(this.rules.get(node.getTextValue()));
            }

            boolean flag = false;
            String spCol = null;

            Map<String, String> valNode = new HashMap<String, String>();
            for (String key : this.specialRoute.keySet()) {
                SpecialRouteConfig spRouteconf = specialRoute.get(key);
                if (spRouteconf.getTableId().equals(id)) {
                    flag = true;
                    valNode.put(spRouteconf.getColumnValue(), spRouteconf.getDatanodeId());
                    spCol = spRouteconf.getColumn();
                }
            }

            if (flag) {
                colValNode.put(spCol, valNode);
                tableConfig = new TableConfig(id, name, dataNodeArray,
                        rules, false, true, colValNode);
            } else {
                tableConfig = new TableConfig(id, name, dataNodeArray,
                        rules, false, false, null);
            }
            if (schemaTables.containsKey(schemaId)) {
                schemaTables.get(schemaId).put(tableConfig.getName(), tableConfig);
            } else {
                tables = new HashMap<String, TableConfig>();
                tables.put(tableConfig.getName(), tableConfig);
                schemaTables.put(schemaId, tables);
            }
        }
    }

    private void loadSchemas() throws ConfigException {
        schemas = new HashMap<String, SchemaConfig>();
        JsonNode schemaNode = rootNode.findValue("schemaInfos");

        if (schemaNode == null) {
            logger.warn("schemaNode is null");
            return;
        }

        String id, name;
        int maxCapacity, maxConns;
        boolean noSharding;
        SchemaConfig schemaConfig = null;
        Map<String, TableConfig> tables = null;

        Iterator<JsonNode> it = schemaNode.iterator();
        while (it.hasNext()) {
            noSharding = false;

            JsonNode jsonNode = it.next();
            id = jsonNode.findValue("id").getTextValue();
            name = jsonNode.findValue("name").getTextValue();
            maxCapacity = jsonNode.findValue("maxCapacity").asInt();
            maxConns = jsonNode.findValue("maxConns").asInt();

            tables = schemaTables.get(id);

            if (tables == null || tables.containsKey(TableConfig.PLACE_HOLDER_TABLE))
                noSharding = true;

            schemaConfig = new SchemaConfig(id, name, schemaTables.get(id), noSharding);
            schemaConfig.setMaxCapacity(maxCapacity);
            schemaConfig.setMaxConns(maxConns);

            if (schemas.containsKey(name)) {
                throw new ConfigException("schemas name " + name + " duplicated!");
            } else {
                schemas.put(name, schemaConfig);
            }
        }
    }

    private void loadTransferSchemas() {

        JsonNode lockSchemaNode = rootNode.findValue("transferSchemas");
        if (lockSchemaNode == null) {
            logger.error("LockSchemaNode is null");
            return;
        }

        String schemaNames = lockSchemaNode.asText();

        if (schemaNames == null || schemaNames.equals("")) {
            return;
        }

        String[] schemaNameArray = schemaNames.split(",");

        for (String name : schemaNameArray) {
            if (schemas.containsKey(name)) {
                schemas.get(name).setLocked(true);
            }
        }
    }

    private void loadUsers() throws ConfigException {
        users = new HashMap<String, UserConfig>();
        JsonNode userNode = rootNode.findValue("userInfos");

        if (userNode == null) {
            logger.error("userNode is null");
            return;
        }

        String id, name, password;
        int privilege, selectMaxRows;
        UserConfig userConfig = null;
        JsonNode jsonNodeArray;

        Iterator<JsonNode> it = userNode.iterator();
        while (it.hasNext()) {
            JsonNode jsonNode = it.next();
            id = jsonNode.findValue("id").getTextValue();
            name = jsonNode.findValue("name").getTextValue();
            password = jsonNode.findValue("password").getTextValue();
            privilege = jsonNode.findValue("readOnly").asInt();
            selectMaxRows = jsonNode.findValue("selectMaxRows").asInt();
            jsonNodeArray = jsonNode.findValue("schemaNames");

            Set<String> schemas = new HashSet<String>();
            for (int index = 0; index < jsonNodeArray.size(); index++) {
                JsonNode node = jsonNodeArray.get(index);
                schemas.add(node.getTextValue());
            }

            userConfig = new UserConfig(id, name);
            userConfig.setPassword(password);
            userConfig.setSchemas(schemas);
            userConfig.setPrivilege(privilege);
            userConfig.setSelectMaxRows(selectMaxRows);

            if (users.containsKey(name)) {
                throw new ConfigException("users name " + name + " duplicated!");
            } else {
                users.put(name, userConfig);
            }
        }
    }

    private void loadManager() {

        JsonNode managerNode = rootNode.findValue("manager");

        if (managerNode == null) {
            logger.error("managerNode is null");
            return;
        }

        String user = managerNode.findValue("userName").asText();
        String passwd = managerNode.findValue("password").asText();
    }

    private void loadPrivileges() {
        // key: upper(userName) value: PrivilegeConfig
        privileges = new HashMap<String, PrivilegeConfig>();
        JsonNode privilegeInfosNodes = rootNode.findValue("privilegeInfos");
        if (privilegeInfosNodes == null) {
            logger.error("privilegeInfos Nodes node is null");
            return;
        }
        Iterator<JsonNode> it = privilegeInfosNodes.iterator();
        PrivilegeConfig privConf;
        String key;
        while (it.hasNext()) {
            privConf = new PrivilegeConfig();
            JsonNode jsonNode = it.next();
            privConf.setUserName(jsonNode.findValue("userName").asText());
            privConf.setInsertPriv(jsonNode.findValue("insertPriv").asBoolean());
            privConf.setDeletePriv(jsonNode.findValue("deletePriv").asBoolean());
            privConf.setUpdatePriv(jsonNode.findValue("updatePriv").asBoolean());
            privConf.setSelectPriv(jsonNode.findValue("selectPriv").asBoolean());
            privConf.setInsertWithShardKeyPriv(jsonNode.findValue("insertWithShardKeyPriv").asBoolean());
            privConf.setDeleteWithShardKeyPriv(jsonNode.findValue("deleteWithShardKeyPriv").asBoolean());
            privConf.setUpdateWithShardKeyPriv(jsonNode.findValue("updateWithShardKeyPriv").asBoolean());
            privConf.setSelectWithShardKeyPriv(jsonNode.findValue("selectWithShardKeyPriv").asBoolean());
            key = PrivilegeConfig.getMapKey(privConf.getUserName());
            privileges.put(key, privConf);
        }
    }

    /**
     * make sure that all processors get the coordinate meta data
     * <p/>
     * nginx way : producing newly processor
     * <p/>
     * rather then using the old worker just waiting previous worker have no connections
     * <p/>
     * then replace all
     *
     * @throws ConfigException
     */
    public void reload() throws ConfigException {
        logger.debug("reload");
        MetaConfig metaConfig = new MetaConfig(this.config);
        this._ruleFuncs = ruleFuncs;
        this._instances = instances;
        this._dataNodes = dataNodes;
        this._schemaTables = schemaTables;
        this._connectionPools = connectionPools;
        this._mySQLDataNodes = mySQLDataNodes;
        this._schemas = schemas;
        this._users = users;
        this._specialRoute = specialRoute;
        this._privileges = privileges;

        this.ruleFuncs = metaConfig.ruleFuncs;
        this.instances = metaConfig.instances;
        this.dataNodes = metaConfig.dataNodes;
        this.schemaTables = metaConfig.schemaTables;
        this.connectionPools = metaConfig.connectionPools;
        this.mySQLDataNodes = metaConfig.mySQLDataNodes;
        this.schemas = metaConfig.schemas;
        this.users = metaConfig.users;
        this.specialRoute = metaConfig.specialRoute;
        this.privileges = metaConfig.privileges;

        metaConfig.reset();
    }

    public Map<String, UserConfig> getUsers() {
        return users;
    }

    public Map<String, PrivilegeConfig> getPrivileges() {
        return privileges;
    }

    public Map<String, DataNodeConfig> getDataNodes() {
        return dataNodes;
    }

    public Map<String, Map<String, TableConfig>> getSchemaTables() {
        return schemaTables;
    }

    public Map<String, SchemaConfig> getSchemas() {
        return schemas;
    }

    public Map<String, InstanceConfig> getInstances() {
        return instances;
    }

    public Map<String, RuleConfig> getRules() {
        return rules;
    }

    public Map<String, MySQLDataNode> getMySQLDataNodes() {
        return mySQLDataNodes;
    }
}
