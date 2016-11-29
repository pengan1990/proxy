/**
 * (created at 2011-9-12)
 */
package parse.ast.stmt.mts;

import parse.ast.expression.primary.Identifier;
import parse.ast.stmt.SQLStatement;
import parse.visitor.SQLASTVisitor;

/**

 */
public class MTSReleaseStatement implements SQLStatement {
    private final Identifier savepoint;

    private StmtType stmtType = StmtType.MTS;

    public MTSReleaseStatement(Identifier savepoint) {
        if (savepoint == null) throw new IllegalArgumentException("savepoint is null");
        this.savepoint = savepoint;
    }

    @Override
    public StmtType getStmtType() {
        return stmtType;
    }

    public Identifier getSavepoint() {
        return savepoint;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
