/**
 * (created at 2011-1-20)
 */
package parse.ast.expression.arithmeic;

import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * <code>higherExpr '/' higherExpr</code>
 */
public class ArithmeticDivideExpression extends ArithmeticBinaryOperatorExpression {
    public ArithmeticDivideExpression(Expression leftOprand, Expression rightOprand) {
        super(leftOprand, rightOprand, PRECEDENCE_ARITHMETIC_FACTOR_OP);
    }

    @Override
    public String getOperator() {
        return "/";
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Number calculate(Integer integer1, Integer integer2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Number calculate(Long long1, Long long2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Number calculate(BigInteger bigint1, BigInteger bigint2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Number calculate(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        throw new UnsupportedOperationException();
    }
}
