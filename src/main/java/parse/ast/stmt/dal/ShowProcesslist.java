/**
 * (created at 2011-5-21)
 */
package parse.ast.stmt.dal;

import parse.visitor.SQLASTVisitor;

/**

 */
public class ShowProcesslist extends DALShowStatement {
    private final boolean full;

    public ShowProcesslist(boolean full) {
        this.full = full;
    }

    public boolean isFull() {
        return full;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
