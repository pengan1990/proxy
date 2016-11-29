/**
 * (created at 2012-8-13)
 */
package parse.ast.fragment.ddl.index;

import parse.ast.ASTNode;
import parse.visitor.SQLASTVisitor;

import java.util.Collections;
import java.util.List;

/**

 */
public class IndexDefinition implements ASTNode {
    private final IndexType indexType;
    private final List<IndexColumnName> columns;
    private final List<IndexOption> options;
    @SuppressWarnings("unchecked")
    public IndexDefinition(IndexType indexType, List<IndexColumnName> columns, List<IndexOption> options) {
        this.indexType = indexType;
        if (columns == null || columns.isEmpty()) throw new IllegalArgumentException("columns is null or empty");
        this.columns = columns;
        this.options = (List<IndexOption>) (options == null || options.isEmpty() ? Collections.emptyList() : options);
    }

    public IndexType getIndexType() {
        return indexType;
    }

    /**
     * @return never null
     */
    public List<IndexColumnName> getColumns() {
        return columns;
    }

    /**
     * @return never null
     */
    public List<IndexOption> getOptions() {
        return options;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        // QS_TODO

    }

    public static enum IndexType {
        BTREE,
        HASH
    }

}
