package parse.ast.stmt.mts;

import parse.ast.stmt.SQLStatement;
import parse.visitor.SQLASTVisitor;

/**
 * Created by pengan on 16-11-22.
 */
public class MTSCommitStatement implements SQLStatement {
    private StmtType stmtType = StmtType.COMMIT;

    public MTSCommitStatement() {
    }

    @Override
    public StmtType getStmtType() {
        return stmtType;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {

    }
}
