/**
 * (created at 2011-2-13)
 */
package parse.ast.expression.primary;

import parse.visitor.SQLASTVisitor;

/**
 * stand for <code>*</code>
 */
public class Wildcard extends Identifier {
    public Wildcard(Identifier parent) {
        super(parent, "*", "*");
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
