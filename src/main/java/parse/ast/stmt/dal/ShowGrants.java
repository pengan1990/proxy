/**
 * (created at 2011-6-3)
 */
package parse.ast.stmt.dal;

import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

/**

 */
public class ShowGrants extends DALShowStatement {
    private final Expression user;

    public ShowGrants(Expression user) {
        this.user = user;
    }

    public ShowGrants() {
        this.user = null;
    }

    public Expression getUser() {
        return user;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
