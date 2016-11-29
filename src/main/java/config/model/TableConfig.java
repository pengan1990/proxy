package config.model;

import config.model.rule.RuleConfig;

import java.util.*;

public class TableConfig {
	
	public final static String PLACE_HOLDER_TABLE = "PLACEHOLDERTABLEBYJD";
	private final String id;
    private final String name;//大写
    private final String orgiName;//原始的表名
    private final String[] dataNodes;//一共有几个datanode
    private final List<RuleConfig> rules;//路由规则
    private final Set<String> routeColumn;//路由字段
    private final boolean ruleRequired;
    private final boolean hasSpecialRule;
    private final Map<String, Map<String, String>> colValNode;
    
    public TableConfig(String id, String name, String[] dataNodes, List<RuleConfig> rules
            , boolean ruleRequired, boolean hasSpecialRule, Map<String, Map<String, String>> colValNode) {

        if (name == null) {
            throw new IllegalArgumentException("table name is null");
        }
        this.id = id;
        this.orgiName = name;
        this.name = name.toUpperCase();
        this.dataNodes = dataNodes;
        if (this.dataNodes == null || this.dataNodes.length <= 0) {
            throw new IllegalArgumentException("invalid table dataNodes: " + dataNodes);
        }

        this.rules = rules;
        if(rules != null && rules.size() == 0 && hasSpecialRule){
            this.routeColumn = colValNode.keySet();
        } else {
            this.routeColumn = buildrouteColumn(rules);
        }

        this.ruleRequired = ruleRequired;
        this.hasSpecialRule = hasSpecialRule;
        this.colValNode = colValNode;
    }

    public boolean existsColumn(String columnNameUp) {
        return routeColumn.contains(columnNameUp);
    }


    public String[] getDataNodes() {
        return dataNodes;
    }

    public boolean isRuleRequired() {
        return ruleRequired;
    }

    private static Set<String> buildrouteColumn(List<RuleConfig> rules) {

        if (rules == null || rules.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> routeColumn = new HashSet<String>();
        for (RuleConfig rule : rules) {
            List<String> columns = rule.getColumns();
            if (columns != null) {
                for (String col : columns) {
                    if (col != null) {
                        routeColumn.add(col.toUpperCase());
                    }
                }
            }
        }
        return routeColumn;
    }

	public String getId() {
		return id;
	}
	
    public List<RuleConfig> getRules() {
        return rules;
    }

	public String getName() {
		return name;
	}

	public String getOrgiName() {
		return orgiName;
	}

    public boolean hasSpecialRule(){
        return hasSpecialRule;
    }

    public Map<String, Map<String, String>> getColValNode(){
        return colValNode;
    }

    public Set<String> getRouteColumn(){
        return routeColumn;
    }
}
