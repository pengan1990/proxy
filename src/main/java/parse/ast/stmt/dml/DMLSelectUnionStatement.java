/**
 * (created at 2011-1-29)
 */
package parse.ast.stmt.dml;

import parse.ast.fragment.Limit;
import parse.ast.fragment.OrderBy;
import parse.ast.stmt.SQLStatement;
import parse.visitor.SQLASTVisitor;

import java.util.LinkedList;
import java.util.List;

/**

 */
public class DMLSelectUnionStatement extends DMLQueryStatement {
    /**
     * might be {@link LinkedList}
     */
    private final List<DMLSelectStatement> selectStmtList;
    /**
     * <code>Mixed UNION types are treated such that a DISTINCT union overrides any ALL union to its left</code>
     * <br/>
     * 0 means all relations of selects are union all<br/>
     * last index of {@link #selectStmtList} means all relations of selects are
     * union distinct<br/>
     */
    private int firstDistinctIndex = 0;
    private OrderBy orderBy;
    private Limit limit;

    public DMLSelectUnionStatement(DMLSelectStatement select) {
        super();
        this.selectStmtList = new LinkedList<DMLSelectStatement>();
        this.selectStmtList.add(select);

        this.stmtType = SQLStatement.StmtType.DML_SELECT_UNION;
    }

    public DMLSelectUnionStatement addSelect(DMLSelectStatement select, boolean unionAll) {
        selectStmtList.add(select);
        if (!unionAll) {
            firstDistinctIndex = selectStmtList.size() - 1;
        }
        return this;
    }

    public List<DMLSelectStatement> getSelectStmtList() {
        return selectStmtList;
    }

    public int getFirstDistinctIndex() {
        return firstDistinctIndex;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public DMLSelectUnionStatement setOrderBy(OrderBy orderBy) {
        this.orderBy = orderBy;
        return this;
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
}
