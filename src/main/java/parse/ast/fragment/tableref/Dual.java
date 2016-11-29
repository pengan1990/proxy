/**
 * (created at 2012-10-24)
 */
package parse.ast.fragment.tableref;

import parse.visitor.SQLASTVisitor;

/**

 */
public class Dual implements TableReference {

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Object removeLastConditionElement() {
        return null;
    }

    @Override
    public boolean isSingleTable() {
        return true;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_FACTOR;
    }

}
