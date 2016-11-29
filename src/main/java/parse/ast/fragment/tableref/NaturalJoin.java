/**
 * (created at 2011-2-9)
 */
package parse.ast.fragment.tableref;

import parse.visitor.SQLASTVisitor;

/**

 */
public class NaturalJoin implements TableReference {
    private final boolean isOuter;
    /**
     * make sense only if {@link #isOuter} is true. Eigher <code>LEFT</code> or
     * <code>RIGHT</code>
     */
    private final boolean isLeft;
    private final TableReference leftTableRef;
    private final TableReference rightTableRef;

    public NaturalJoin(boolean isOuter, boolean isLeft, TableReference leftTableRef, TableReference rightTableRef) {
        super();
        this.isOuter = isOuter;
        this.isLeft = isLeft;
        this.leftTableRef = leftTableRef;
        this.rightTableRef = rightTableRef;
    }

    public boolean isOuter() {
        return isOuter;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public TableReference getLeftTableRef() {
        return leftTableRef;
    }

    public TableReference getRightTableRef() {
        return rightTableRef;
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
