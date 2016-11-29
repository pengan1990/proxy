/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.datetime;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;
import parse.ast.expression.primary.literal.IntervalPrimary;
import parse.visitor.SQLASTVisitor;

import java.util.List;

/**

 */
public class Extract extends FunctionExpression {
    private IntervalPrimary.Unit unit;

    public Extract(IntervalPrimary.Unit unit, Expression date) {
        super("EXTRACT", wrapList(date));
        this.unit = unit;
    }

    public IntervalPrimary.Unit getUnit() {
        return unit;
    }

    public Expression getDate() {
        return arguments.get(0);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        throw new UnsupportedOperationException("function of extract has special arguments");
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
