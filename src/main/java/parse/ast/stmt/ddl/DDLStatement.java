/**
 * (created at 2011-7-4)
 */
package parse.ast.stmt.ddl;

import parse.ast.stmt.SQLStatement;

/**
 * NOT FULL AST
 */
//public interface DDLStatement extends SQLStatement {
public abstract class DDLStatement implements SQLStatement {
    //QS_TODO ddl regenerate sql by router
    protected StmtType stmtType = StmtType.DDL;

    @Override
    public StmtType getStmtType() {
        return stmtType;
    }
}
