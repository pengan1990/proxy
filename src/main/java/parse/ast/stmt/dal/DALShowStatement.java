/**
 * (created at 2011-5-20)
 */
package parse.ast.stmt.dal;

import parse.ast.stmt.SQLStatement;

/**
 * @author zhangchengyuan
 */
public abstract class DALShowStatement implements SQLStatement {
    protected StmtType stmtType = StmtType.DAL_SHOW;

    @Override
    public StmtType getStmtType() {
        return stmtType;
    }
}
