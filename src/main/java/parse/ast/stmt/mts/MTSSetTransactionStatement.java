/**
 * (created at 2011-6-8)
 */
package parse.ast.stmt.mts;

import parse.ast.fragment.VariableScope;
import parse.ast.stmt.SQLStatement;
import parse.visitor.SQLASTVisitor;

/**

 */
public class MTSSetTransactionStatement implements SQLStatement {
    private final VariableScope scope;
    private final IsolationLevel level;
    private StmtType stmtType = StmtType.MTS;

    public MTSSetTransactionStatement(VariableScope scope, IsolationLevel level) {
        super();
        if (level == null) throw new IllegalArgumentException("isolation level is null");
        this.level = level;
        this.scope = scope;
    }

    @Override
    public StmtType getStmtType() {
        return stmtType;
    }

    /**
     * @retern null means scope undefined
     */
    public VariableScope getScope() {
        return scope;
    }

    public IsolationLevel getLevel() {
        return level;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public static enum IsolationLevel {
        READ_UNCOMMITTED,
        READ_COMMITTED,
        REPEATABLE_READ,
        SERIALIZABLE
    }
}
