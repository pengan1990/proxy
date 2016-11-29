/**
 * (created at 2011-7-20)
 */
package parse.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**

 */
public interface BinaryOperandCalculator {
    Number calculate(Integer integer1, Integer integer2);

    Number calculate(Long long1, Long long2);

    Number calculate(BigInteger bigint1, BigInteger bigint2);

    Number calculate(BigDecimal bigDecimal1, BigDecimal bigDecimal2);
}
