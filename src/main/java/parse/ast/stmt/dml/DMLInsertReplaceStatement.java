/**
 * (created at 2011-7-31)
 */
package parse.ast.stmt.dml;

import parse.ast.expression.misc.QueryExpression;
import parse.ast.expression.primary.Identifier;
import parse.ast.expression.primary.RowExpression;
import parse.ast.stmt.SQLStatement;

import java.util.List;

/**

 */
public abstract class DMLInsertReplaceStatement extends DMLStatement {
    protected final Identifier table;
    protected final List<Identifier> columnNameList;
    protected final QueryExpression select;
    protected List<RowExpression> rowList;
    private List<RowExpression> rowListBak;

    @SuppressWarnings("unchecked")
    public DMLInsertReplaceStatement(Identifier table, List<Identifier> columnNameList, List<RowExpression> rowList) {
        this.table = table;
        this.columnNameList = ensureListType(columnNameList);
        this.rowList = ensureListType(rowList);
        this.select = null;

        this.stmtType = SQLStatement.StmtType.DML_INSERT;
    }

    @SuppressWarnings("unchecked")
    public DMLInsertReplaceStatement(Identifier table, List<Identifier> columnNameList, QueryExpression select) {
        if (select == null) throw new IllegalArgumentException("argument 'select' is empty");
        this.select = select;
        this.table = table;
        this.columnNameList = ensureListType(columnNameList);
        this.rowList = null;

        this.stmtType = SQLStatement.StmtType.DML_INSERT;
    }

    public Identifier getTable() {
        return table;
    }

    /**
     * @return {@link java.util.ArrayList ArrayList}
     */
    public List<Identifier> getColumnNameList() {
        return columnNameList;
    }

    /**
     * @return {@link java.util.ArrayList ArrayList} or
     * {@link java.util.Collections#emptyList() EMPTY_LIST}
     */
    public List<RowExpression> getRowList() {
        return rowList;
    }

    public QueryExpression getSelect() {
        return select;
    }

    public void setReplaceRowList(List<RowExpression> list) {
        rowListBak = rowList;
        rowList = list;
    }

    public void clearReplaceRowList() {
        if (rowListBak != null) {
            rowList = rowListBak;
            rowListBak = null;
        }
    }

}
