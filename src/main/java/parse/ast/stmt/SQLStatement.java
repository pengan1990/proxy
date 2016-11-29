/**
 * (created at 2011-2-18)
 */
package parse.ast.stmt;

import parse.ast.ASTNode;

/**
 * should filter some special sql type
 */
public interface SQLStatement extends ASTNode {
    public StmtType getStmtType();//添加类型

    public static enum StmtType {

        // here are to intercept
        DAL_SHOW_DATABASES,
        DAL_SET,
        DML_KILL,
        DML_USE,
        //
        DML_SELECT,
        DML_SELECT_UNION,
        DML_DELETE,
        DML_INSERT,
        DML_REPLACE,
        DML_UPDATE,
        DML_CALL,
        DAL_SHOW,
        MTL_START,
        /**
         * COMMIT or ROLLBACK
         */
        MTL_TERMINATE,
        MTL_ISOLATION,
        DDL,//add
        DSEC,
        MTS,
        /**
         *
         * commit begin rollback
         */
        COMMIT,
        BEGIN,
        ROLLBACK,
    }
}
