/**
 * (created at 2011-9-12)
 */
package parse.ast.stmt.mts;

import parse.ast.expression.primary.Identifier;
import parse.ast.stmt.SQLStatement;
import parse.visitor.SQLASTVisitor;

/**

 */
public class MTSRollbackStatement implements SQLStatement {

    private final CompleteType completeType;
    private final Identifier savepoint;
    private StmtType stmtType = StmtType.ROLLBACK;

    public MTSRollbackStatement(CompleteType completeType) {
        if (completeType == null) throw new IllegalArgumentException("complete type is null!");
        this.completeType = completeType;
        this.savepoint = null;
    }
    public MTSRollbackStatement(Identifier savepoint) {
        this.completeType = null;
        if (savepoint == null) throw new IllegalArgumentException("savepoint is null!");
        this.savepoint = savepoint;
    }

    @Override
    public StmtType getStmtType() {
        return stmtType;
    }

    /**
     * @return null if roll back to SAVEPOINT
     */
    public CompleteType getCompleteType() {
        return completeType;
    }

    /**
     * @return null for roll back the whole transaction
     */
    public Identifier getSavepoint() {
        return savepoint;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public static enum CompleteType {
        /**
         * not specified, then use default
         */
        UN_DEF,
        CHAIN,
        /**
         * MySQL's default
         */
        NO_CHAIN,
        RELEASE,
        NO_RELEASE
    }

}
