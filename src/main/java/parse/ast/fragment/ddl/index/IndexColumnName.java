/**
 * (created at 2012-8-13)
 */
package parse.ast.fragment.ddl.index;

import parse.ast.ASTNode;
import parse.ast.expression.Expression;
import parse.ast.expression.primary.Identifier;
import parse.visitor.SQLASTVisitor;

/**

 */
public class IndexColumnName implements ASTNode {
    private final Identifier columnName;
    /**
     * null is possible
     */
    private final Expression length;
    private final boolean asc;

    public IndexColumnName(Identifier columnName, Expression length, boolean asc) {
        this.columnName = columnName;
        this.length = length;
        this.asc = asc;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public Identifier getColumnName() {
        return columnName;
    }

    public Expression getLength() {
        return length;
    }

    public boolean isAsc() {
        return asc;
    }

}
