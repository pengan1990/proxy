/**
 * (created at 2011-7-4)
 */
package parse.ast.stmt.ddl;

import parse.ast.expression.primary.Identifier;
import parse.visitor.SQLASTVisitor;

/**

 */
public class DDLTruncateStatement extends DDLStatement {
    private final Identifier table;

    public DDLTruncateStatement(Identifier table) {
        this.table = table;
    }

    public Identifier getTable() {
        return table;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
