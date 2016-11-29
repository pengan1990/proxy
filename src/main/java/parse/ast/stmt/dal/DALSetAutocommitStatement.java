package parse.ast.stmt.dal;

import parse.ast.stmt.SQLStatement;
import parse.visitor.SQLASTVisitor;

/**
 * Created by pengan on 16-11-24.
 * <p/>
 * set autocommit = 1 / true ==> commit according to hypothesis 0->1
 * set autocommit = 0 / false ==> begin according to hypothesis 1->0
 */
public class DALSetAutocommitStatement implements SQLStatement {
    private static final String NUM_TRUE = "1";
    private static final String STRING_TRUE = "TRUE";
    private StmtType stmtType = StmtType.DAL_SET;
    private boolean autocommit;

    public DALSetAutocommitStatement(String autocommit) {
        if (NUM_TRUE.equals(autocommit) ||
                STRING_TRUE.equals(autocommit)) {
            this.autocommit = true;
        } else {
            this.autocommit = false;
        }
    }

    @Override
    public StmtType getStmtType() {
        return this.stmtType;
    }

    public boolean isAutocommit() {
        return autocommit;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {

    }
}
