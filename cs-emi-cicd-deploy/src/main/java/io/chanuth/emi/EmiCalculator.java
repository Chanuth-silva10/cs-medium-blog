package io.chanuth.emi;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/** The standard loan formula: EMI = P * r * (1 + r)^n / ((1 + r)^n - 1), with r the monthly rate. */
@Component
public class EmiCalculator {

    private static final MathContext PRECISION = new MathContext(20, RoundingMode.HALF_EVEN);
    private static final BigDecimal MONTHS_PER_YEAR = BigDecimal.valueOf(12);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public record EmiResult(BigDecimal monthlyPayment, BigDecimal totalPayable, BigDecimal totalInterest) {
    }

    /**
     * @param principal  the loan amount
     * @param annualRate the yearly interest rate in percent, 0 means an interest free loan
     * @param months     the number of monthly payments
     */
    public EmiResult calculate(BigDecimal principal, BigDecimal annualRate, int months) {
        if (principal.signum() <= 0 || annualRate.signum() < 0 || months <= 0) {
            throw new IllegalArgumentException("The amount and the months must be positive, the rate cannot be negative");
        }
        BigDecimal monthly;
        if (annualRate.signum() == 0) {
            monthly = principal.divide(BigDecimal.valueOf(months), PRECISION);
        } else {
            BigDecimal rate = annualRate.divide(MONTHS_PER_YEAR, PRECISION).divide(HUNDRED, PRECISION);
            BigDecimal growth = BigDecimal.ONE.add(rate).pow(months, PRECISION);
            monthly = principal.multiply(rate).multiply(growth).divide(growth.subtract(BigDecimal.ONE), PRECISION);
        }
        BigDecimal monthlyPayment = monthly.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPayable = monthlyPayment.multiply(BigDecimal.valueOf(months));
        BigDecimal totalInterest = totalPayable.subtract(principal.setScale(2, RoundingMode.HALF_UP));
        return new EmiResult(monthlyPayment, totalPayable, totalInterest);
    }
}

