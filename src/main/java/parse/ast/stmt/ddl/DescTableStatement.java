/**
 * (created at 2011-8-11)
 */
package parse.ast.stmt.ddl;

import parse.ast.expression.primary.Identifier;
import parse.ast.stmt.SQLStatement;
import parse.visitor.SQLASTVisitor;

/**

 */
public class DescTableStatement implements SQLStatement {
    private final Identifier table;

    private StmtType stmtType = StmtType.DSEC;

    public DescTableStatement(Identifier table) {
        if (table == null) throw new IllegalArgumentException("table is null for desc table");
        this.table = table;

    }

    @Override
    public StmtType getStmtType() {
        return stmtType;
    }

    public Identifier getTable() {
        return table;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
