package config.model.rule;

import java.util.Map;

public interface RuleAlgorithm {

    RuleAlgorithm constructMe(Object... objects);

    void initialize();

    /**
     * @return never null
     */
    Integer[] calculate(Map<? extends Object, ? extends Object> parameters);
}
