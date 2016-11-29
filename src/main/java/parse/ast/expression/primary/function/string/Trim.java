/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.string;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;
import parse.visitor.SQLASTVisitor;

import java.util.ArrayList;
import java.util.List;

/**

 */
public class Trim extends FunctionExpression {
    private final Direction direction;

    public Trim(Direction direction, Expression remstr, Expression str) {
        super("TRIM", wrapList(str, remstr));
        this.direction = direction;
    }

    private static List<Expression> wrapList(Expression str, Expression remstr) {
        if (str == null) throw new IllegalArgumentException("str is null");
        List<Expression> list = remstr != null ? new ArrayList<Expression>(2) : new ArrayList<Expression>(1);
        list.add(str);
        if (remstr != null) list.add(remstr);
        return list;
    }

    /**
     * @return never null
     */
    public Expression getString() {
        return getArguments().get(0);
    }

    public Expression getRemainString() {
        List<Expression> args = getArguments();
        if (args.size() < 2) return null;
        return getArguments().get(1);
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        throw new UnsupportedOperationException("function of trim has special arguments");
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public static enum Direction {
        /**
         * no tag for direction
         */
        DEFAULT,
        BOTH,
        LEADING,
        TRAILING
    }
}
