/**
 * (created at 2011-5-18)
 */
package parse.ast.stmt.dml;

import parse.ast.expression.Expression;
import parse.ast.expression.misc.QueryExpression;
import parse.ast.expression.primary.Identifier;
import parse.ast.expression.primary.RowExpression;
import parse.util.Pair;
import parse.visitor.SQLASTVisitor;

import java.util.List;

/**

 */
public class DMLInsertStatement extends DMLInsertReplaceStatement {
    private final InsertMode mode;
    private final boolean ignore;
    private final List<Pair<Identifier, Expression>> duplicateUpdate;
    /**
     * (insert ... values | insert ... set) format
     *
     * @param columnNameList can be null
     */
    @SuppressWarnings("unchecked")
    public DMLInsertStatement(InsertMode mode, boolean ignore, Identifier table, List<Identifier> columnNameList,
                              List<RowExpression> rowList, List<Pair<Identifier, Expression>> duplicateUpdate) {
        super(table, columnNameList, rowList);
        this.mode = mode;
        this.ignore = ignore;
        this.duplicateUpdate = ensureListType(duplicateUpdate);
    }

    /**
     * insert ... select format
     *
     * @param columnNameList can be null
     */
    @SuppressWarnings("unchecked")
    public DMLInsertStatement(InsertMode mode, boolean ignore, Identifier table, List<Identifier> columnNameList,
                              QueryExpression select, List<Pair<Identifier, Expression>> duplicateUpdate) {
        super(table, columnNameList, select);
        this.mode = mode;
        this.ignore = ignore;
        this.duplicateUpdate = ensureListType(duplicateUpdate);
    }

    public InsertMode getMode() {
        return mode;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public List<Pair<Identifier, Expression>> getDuplicateUpdate() {
        return duplicateUpdate;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public static enum InsertMode {
        /**
         * default
         */
        UNDEF,
        LOW,
        DELAY,
        HIGH
    }
}
