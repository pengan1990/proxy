/**
 * (created at 2011-4-12)
 */
package parse.ast.expression.primary.function;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.PrimaryExpression;
import parse.visitor.SQLASTVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**

 */
public abstract class FunctionExpression extends PrimaryExpression {
    protected final String functionName;
    protected final List<Expression> arguments;

    public FunctionExpression(String functionName, List<Expression> arguments) {
        super();
        this.functionName = functionName;
        if (arguments == null || arguments.isEmpty()) {
            this.arguments = Collections.emptyList();
        } else {
            if (arguments instanceof ArrayList) {
                this.arguments = arguments;
            } else {
                this.arguments = new ArrayList<Expression>(arguments);
            }
        }
    }

    protected static List<Expression> wrapList(Expression expr) {
        List<Expression> list = new ArrayList<Expression>(1);
        list.add(expr);
        return list;
    }

    /**
     * <code>this</code> function object being called is a prototype
     */
    public abstract FunctionExpression constructFunction(List<Expression> arguments);

    public void init() {
    }

    /**
     * @return never null
     */
    public List<Expression> getArguments() {
        return arguments;
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public Expression setCacheEvalRst(boolean cacheEvalRst) {
        return super.setCacheEvalRst(cacheEvalRst);
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
