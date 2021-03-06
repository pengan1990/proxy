/**
 * (created at 2012-8-13)
 */
package parse.ast.fragment.ddl.index;

import parse.ast.ASTNode;
import parse.ast.expression.Expression;
import parse.ast.expression.primary.Identifier;
import parse.ast.expression.primary.literal.LiteralString;
import parse.visitor.SQLASTVisitor;

/**

 */
public class IndexOption implements ASTNode {
    private final Expression keyBlockSize;
    private final IndexType indexType;
    private final Identifier parserName;
    private final LiteralString comment;
    public IndexOption(Expression keyBlockSize) {
        this.keyBlockSize = keyBlockSize;
        this.indexType = null;
        this.parserName = null;
        this.comment = null;
    }

    public IndexOption(IndexType indexType) {
        this.keyBlockSize = null;
        this.indexType = indexType;
        this.parserName = null;
        this.comment = null;
    }

    public IndexOption(Identifier parserName) {
        this.keyBlockSize = null;
        this.indexType = null;
        this.parserName = parserName;
        this.comment = null;
    }

    public IndexOption(LiteralString comment) {
        this.keyBlockSize = null;
        this.indexType = null;
        this.parserName = null;
        this.comment = comment;
    }

    public Expression getKeyBlockSize() {
        return keyBlockSize;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public Identifier getParserName() {
        return parserName;
    }

    public LiteralString getComment() {
        return comment;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public static enum IndexType {
        BTREE,
        HASH
    }

}
