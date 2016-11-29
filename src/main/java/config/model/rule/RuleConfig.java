package config.model.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RuleConfig {

	private final String id;
    private final List<String> columns;//路由字段，可能涉及多个
    private RuleAlgorithm ruleAlgorithm;//路由算法

    public RuleConfig(String id, String[] columns, RuleAlgorithm ruleAlgorithm) {

    	this.id = id;

        if (columns == null || columns.length <= 0) {
            throw new IllegalArgumentException("no rule column is found");
        }

        List<String> list = new ArrayList<String>(columns.length);

        for (String column : columns) {
            if (column == null) {
                throw new IllegalArgumentException("column value is null: " + columns);
            }
            list.add(column.toUpperCase());
        }
        this.columns = Collections.unmodifiableList(list);
        this.ruleAlgorithm = ruleAlgorithm;
    }

    public RuleAlgorithm getRuleAlgorithm() {
        return ruleAlgorithm;
    }

    public void setRuleAlgorithm(RuleAlgorithm ruleAlgorithm) {
        this.ruleAlgorithm = ruleAlgorithm;
    }

    /**
     * @return unmodifiable, upper-case
     */
    public List<String> getColumns() {
        return columns;
    }

	public String getId() {
		return id;
	}
}
