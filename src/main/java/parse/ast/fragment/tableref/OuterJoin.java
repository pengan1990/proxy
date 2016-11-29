/**
 * (created at 2011-2-9)
 */
package parse.ast.fragment.tableref;

import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * left or right join
 */
public class OuterJoin implements TableReference {
    /**
     * for MySQL, only <code>LEFT</code> and <code>RIGHT</code> outer join are
     * supported
     */
    private final boolean isLeftJoin;
    private final TableReference leftTableRef;
    private final TableReference rightTableRef;
    private final Expression onCond;
    private final List<String> using;
    private OuterJoin(boolean isLeftJoin, TableReference leftTableRef, TableReference rightTableRef, Expression onCond,
                      List<String> using) {
        super();
        this.isLeftJoin = isLeftJoin;
        this.leftTableRef = leftTableRef;
        this.rightTableRef = rightTableRef;
        this.onCond = onCond;
        this.using = ensureListType(using);
    }

    public OuterJoin(boolean isLeftJoin, TableReference leftTableRef, TableReference rightTableRef, Expression onCond) {
        this(isLeftJoin, leftTableRef, rightTableRef, onCond, null);
    }

    public OuterJoin(boolean isLeftJoin, TableReference leftTableRef, TableReference rightTableRef, List<String> using) {
        this(isLeftJoin, leftTableRef, rightTableRef, null, using);
    }

    private static List<String> ensureListType(List<String> list) {
        if (list == null) return null;
        if (list.isEmpty()) return Collections.emptyList();
        if (list instanceof ArrayList) return list;
        return new ArrayList<String>(list);
    }

    public boolean isLeftJoin() {
        return isLeftJoin;
    }

    public TableReference getLeftTableRef() {
        return leftTableRef;
    }

    public TableReference getRightTableRef() {
        return rightTableRef;
    }

    public Expression getOnCond() {
        return onCond;
    }

    public List<String> getUsing() {
        return using;
    }

    @Override
    public Object removeLastConditionElement() {
        return null;
    }

    @Override
    public boolean isSingleTable() {
        return false;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_JOIN;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
