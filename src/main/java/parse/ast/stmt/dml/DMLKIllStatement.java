package parse.ast.stmt.dml;

import parse.ast.expression.primary.Identifier;
import parse.visitor.SQLASTVisitor;

/**
 * Created by pengan on 16-11-4.
 */
public class DMLKIllStatement extends DMLStatement {

    private long connectionId;

    public DMLKIllStatement(long connectionId) {
        this.connectionId = connectionId;
        this.stmtType = StmtType.DML_KILL;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        // no accept
    }

    public long getConnectionId() {
        return connectionId;
    }
}
