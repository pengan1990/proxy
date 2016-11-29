/**
 * (created at 2011-5-20)
 */
package parse.ast.stmt.dal;

import parse.visitor.SQLASTVisitor;

/**

 */
public class ShowAuthors extends DALShowStatement {
    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
