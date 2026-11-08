package io.chanuth.emi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.chanuth.emi.EmiCalculator.EmiResult;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class EmiCalculatorTest {

    private final EmiCalculator calculator = new EmiCalculator();

    @Test
    void aTypicalLoanMatchesTheKnownFigure() {
        // 100,000 at 12% a year over 12 months is the textbook example: 8,884.88 a month.
        EmiResult result = calculator.calculate(new BigDecimal("100000"), new BigDecimal("12"), 12);

        assertThat(result.monthlyPayment()).isEqualByComparingTo("8884.88");
        assertThat(result.totalPayable()).isEqualByComparingTo("106618.56");
        assertThat(result.totalInterest()).isEqualByComparingTo("6618.56");
    }

    @Test
    void aLongHomeLoan() {
        // 5,000,000 at 8.5% over 20 years (240 months).
        EmiResult result = calculator.calculate(new BigDecimal("5000000"), new BigDecimal("8.5"), 240);

        assertThat(result.monthlyPayment()).isEqualByComparingTo("43391.16");
    }

    @Test
    void anInterestFreeLoanIsJustTheAmountDividedByTheMonths() {
        EmiResult result = calculator.calculate(new BigDecimal("12000"), BigDecimal.ZERO, 12);

        assertThat(result.monthlyPayment()).isEqualByComparingTo("1000.00");
        assertThat(result.totalInterest()).isEqualByComparingTo("0.00");
    }

    @Test
    void impossibleInputIsRefused() {
        assertThatThrownBy(() -> calculator.calculate(BigDecimal.ZERO, BigDecimal.TEN, 12))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calculator.calculate(BigDecimal.TEN, BigDecimal.valueOf(-1), 12))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calculator.calculate(BigDecimal.TEN, BigDecimal.TEN, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

