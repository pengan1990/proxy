/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.datetime;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;
import parse.visitor.SQLASTVisitor;

import java.util.List;

/**

 */
public class GetFormat extends FunctionExpression {
    private FormatType formatType;

    public GetFormat(FormatType type, Expression format) {
        super("GET_FORMAT", wrapList(format));
        this.formatType = type;
    }

    public FormatType getFormatType() {
        return formatType;
    }

    public Expression getFormat() {
        return arguments.get(0);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        throw new UnsupportedOperationException("function of GetFormat has special arguments");
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public static enum FormatType {
        DATE,
        TIME,
        DATETIME
    }

}
