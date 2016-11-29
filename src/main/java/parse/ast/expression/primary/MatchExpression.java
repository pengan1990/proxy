/**
 * (created at 2011-1-22)
 */
package parse.ast.expression.primary;

import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**

 */
public class MatchExpression extends PrimaryExpression {
    private final List<Expression> columns;
    private final Expression pattern;
    private final Modifier modifier;
    /**
     * @param modifier never null
     */
    public MatchExpression(List<Expression> columns, Expression pattern, Modifier modifier) {
        if (columns == null || columns.isEmpty()) {
            this.columns = Collections.emptyList();
        } else if (columns instanceof ArrayList) {
            this.columns = columns;
        } else {
            this.columns = new ArrayList<Expression>(columns);
        }
        this.pattern = pattern;
        this.modifier = modifier;
    }

    public List<Expression> getColumns() {
        return columns;
    }

    public Expression getPattern() {
        return pattern;
    }

    public Modifier getModifier() {
        return modifier;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public static enum Modifier {
        /**
         * no modifier
         */
        _DEFAULT,
        IN_BOOLEAN_MODE,
        IN_NATURAL_LANGUAGE_MODE,
        IN_NATURAL_LANGUAGE_MODE_WITH_QUERY_EXPANSION,
        WITH_QUERY_EXPANSION
    }
}
