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
public class Timestampadd extends FunctionExpression {
    private IntervalPrimary.Unit unit;

    public Timestampadd(IntervalPrimary.Unit unit, List<Expression> arguments) {
        super("TIMESTAMPADD", arguments);
        this.unit = unit;
    }

    public IntervalPrimary.Unit getUnit() {
        return unit;
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        throw new UnsupportedOperationException("function of Timestampadd has special arguments");
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
