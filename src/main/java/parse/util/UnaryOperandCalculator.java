/**
 * (created at 2011-7-20)
 */
package parse.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**

 */
public interface UnaryOperandCalculator {
    Number calculate(Integer num);

    Number calculate(Long num);

    Number calculate(BigInteger num);

    Number calculate(BigDecimal num);
}
