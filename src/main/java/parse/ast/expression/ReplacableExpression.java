/**
 * (created at 2011-8-1)
 */
package parse.ast.expression;

import parse.ast.expression.primary.literal.LiteralBoolean;

/**

 */
public interface ReplacableExpression extends Expression {
    LiteralBoolean BOOL_FALSE = new LiteralBoolean(false);

    void setReplaceExpr(Expression replaceExpr);

    void clearReplaceExpr();
}
