/**
 * (created at 2011-9-23)
 */
package parse.ast.stmt.dal;

import parse.ast.stmt.SQLStatement;
import parse.visitor.SQLASTVisitor;

/**

 */
public class DALSetCharacterSetStatement implements SQLStatement {
    private final String charset;

    private StmtType stmtType = StmtType.DAL_SET;

    public DALSetCharacterSetStatement() {
        this.charset = null;
    }

    public DALSetCharacterSetStatement(String charset) {
        if (charset == null) throw new IllegalArgumentException("charsetName is null");
        this.charset = charset;
    }

    @Override
    public StmtType getStmtType() {
        return stmtType;
    }

    public boolean isDefault() {
        return charset == null;
    }

    public String getCharset() {
        return charset;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
