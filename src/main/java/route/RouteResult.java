package route;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by pengan on 16-9-29.
 */
public class RouteResult {

    private StringBuilder originalSql;
    // datanode id , sql
    private final Map<String, StringBuilder> nodeSqls;

    public RouteResult() {
        this.originalSql = new StringBuilder();
        nodeSqls = new LinkedHashMap<String, StringBuilder>();
    }

    public Map<String, StringBuilder> getNodeSqls() {
        return nodeSqls;
    }

    public StringBuilder getOriginalSql() {
        return originalSql;
    }

    public void setOriginalSql(StringBuilder originalSql) {
        this.originalSql.append(originalSql);
    }

    /**
     * attention: if all node sql share the same memory  one change another
     *
     * @param sql
     */
    public void setNodeSql(StringBuilder sql) {
        for (Map.Entry<String, StringBuilder> entry : nodeSqls.entrySet()) {
            entry.getValue().setLength(0);
            entry.getValue().append(sql);
            break;
        }
    }

    public void reset() {
        this.originalSql.setLength(0);
        for (Map.Entry<String, StringBuilder> entry : nodeSqls.entrySet()) {
            entry.getValue().setLength(0);
        }
        nodeSqls.clear();
    }
}
