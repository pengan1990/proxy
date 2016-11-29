/**
 * (created at 2011-1-20)
 */
package parse.ast.expression.arithmeic;

import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * <code>higherExpr ('MOD'|'%') higherExpr</code>
 */
public class ArithmeticModExpression extends ArithmeticBinaryOperatorExpression {
    public ArithmeticModExpression(Expression leftOprand, Expression rightOprand) {
        super(leftOprand, rightOprand, PRECEDENCE_ARITHMETIC_FACTOR_OP);
    }

    @Override
    public String getOperator() {
        return "%";
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Number calculate(Integer integer1, Integer integer2) {
        if (integer1 == null || integer2 == null) return null;
        int i1 = integer1.intValue();
        int i2 = integer2.intValue();
        if (i2 == 0) return null;
        return i1 % i2;
    }

    @Override
    public Number calculate(Long long1, Long long2) {
        if (long1 == null || long2 == null) return null;
        int i1 = long1.intValue();
        int i2 = long2.intValue();
        if (i2 == 0) return null;
        return i1 % i2;
    }

    @Override
    public Number calculate(BigInteger bigint1, BigInteger bigint2) {
        if (bigint1 == null || bigint2 == null) return null;
        int comp = bigint2.compareTo(BigInteger.ZERO);
        if (comp == 0) {
            return null;
        } else if (comp < 0) {
            return bigint1.negate().mod(bigint2).negate();
        } else {
            return bigint1.mod(bigint2);
        }
    }

    @Override
    public Number calculate(BigDecimal bigDecimal1, BigDecimal bigDecimal2) {
        throw new UnsupportedOperationException();
    }
}
