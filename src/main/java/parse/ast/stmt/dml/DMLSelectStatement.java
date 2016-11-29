/**
 * (created at 2011-1-28)
 */
package parse.ast.stmt.dml;

import parse.ast.expression.Expression;
import parse.ast.fragment.GroupBy;
import parse.ast.fragment.Limit;
import parse.ast.fragment.OrderBy;
import parse.ast.fragment.tableref.TableReferences;
import parse.ast.stmt.SQLStatement;
import parse.util.Pair;
import parse.visitor.SQLASTVisitor;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**

 */
public class DMLSelectStatement extends DMLQueryStatement {
    private final SelectOption option;
    /**
     * string: id | `id` | 'id'
     */
    private final List<Pair<Expression, String>> selectExprList;
    private final TableReferences tables;
    private final Expression where;
    private final GroupBy group;
    private final Expression having;
    private OrderBy order;
    private Limit limit;
    /**
     * @throws SQLSyntaxErrorException
     */
    @SuppressWarnings("unchecked")
    public DMLSelectStatement(SelectOption option, List<Pair<Expression, String>> selectExprList,
                              TableReferences tables, Expression where, GroupBy group, Expression having,
                              OrderBy order, Limit limit) {
        if (option == null) throw new IllegalArgumentException("argument 'option' is null");
        this.option = option;
        if (selectExprList == null || selectExprList.isEmpty()) {
            this.selectExprList = Collections.emptyList();
        } else {
            this.selectExprList = ensureListType(selectExprList);
        }
        this.tables = tables;
        this.where = where;
        this.group = group;
        this.having = having;
        this.order = order;
        this.limit = limit;

        this.stmtType = SQLStatement.StmtType.DML_SELECT;
    }

    public SelectOption getOption() {
        return option;
    }

    /**
     * @return never null
     */
    public List<Pair<Expression, String>> getSelectExprList() {
        return selectExprList;
    }

    /**
     * @performance slow
     */
    public List<Expression> getSelectExprListWithoutAlias() {
        if (selectExprList == null || selectExprList.isEmpty()) return Collections.emptyList();
        List<Expression> list = new ArrayList<Expression>(selectExprList.size());
        for (Pair<Expression, String> p : selectExprList) {
            if (p != null && p.getKey() != null) {
                list.add(p.getKey());
            }
        }
        return list;
    }

    public TableReferences getTables() {
        return tables;
    }

    public Expression getWhere() {
        return where;
    }

    public GroupBy getGroup() {
        return group;
    }

    public Expression getHaving() {
        return having;
    }

    public OrderBy getOrder() {
        return order;
    }

    public void setOrder(OrderBy order) {
        this.order = order;
    }

    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public static enum SelectDuplicationStrategy {
        /**
         * default
         */
        ALL,
        DISTINCT,
        DISTINCTROW
    }

    public static enum QueryCacheStrategy {
        UNDEF,
        SQL_CACHE,
        SQL_NO_CACHE
    }

    public static enum SmallOrBigResult {
        UNDEF,
        SQL_SMALL_RESULT,
        SQL_BIG_RESULT
    }

    public static enum LockMode {
        UNDEF,
        FOR_UPDATE,
        LOCK_IN_SHARE_MODE
    }

    public static final class SelectOption {
        public SelectDuplicationStrategy resultDup = SelectDuplicationStrategy.ALL;
        public boolean highPriority = false;
        public boolean straightJoin = false;
        public SmallOrBigResult resultSize = SmallOrBigResult.UNDEF;
        public boolean sqlBufferResult = false;
        public QueryCacheStrategy queryCache = QueryCacheStrategy.UNDEF;
        public boolean sqlCalcFoundRows = false;
        public LockMode lockMode = LockMode.UNDEF;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(getClass().getSimpleName()).append('{');
            sb.append("resultDup").append('=').append(resultDup.name());
            sb.append(", ").append("highPriority").append('=').append(highPriority);
            sb.append(", ").append("straightJoin").append('=').append(straightJoin);
            sb.append(", ").append("resultSize").append('=').append(resultSize.name());
            sb.append(", ").append("sqlBufferResult").append('=').append(sqlBufferResult);
            sb.append(", ").append("queryCache").append('=').append(queryCache.name());
            sb.append(", ").append("sqlCalcFoundRows").append('=').append(sqlCalcFoundRows);
            sb.append(", ").append("lockMode").append('=').append(lockMode.name());
            sb.append('}');
            return sb.toString();
        }
    }
}
