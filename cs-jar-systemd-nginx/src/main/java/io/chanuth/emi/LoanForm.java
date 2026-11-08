package io.chanuth.emi;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** What the visitor types in the form. A normal class, because Thymeleaf reads and writes it by getter and setter. */
public class LoanForm {

    @NotNull(message = "Enter the loan amount")
    @DecimalMin(value = "1000", message = "The amount must be at least 1,000")
    @DecimalMax(value = "100000000", message = "The amount cannot be above 100,000,000")
    private BigDecimal amount;

    @NotNull(message = "Enter the yearly interest rate")
    @DecimalMin(value = "0", message = "The rate cannot be negative")
    @DecimalMax(value = "60", message = "The rate cannot be above 60%")
    private BigDecimal rate;

    @NotNull(message = "Enter the number of months")
    @Min(value = 1, message = "At least 1 month")
    @Max(value = 480, message = "At most 480 months (40 years)")
    private Integer months;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public Integer getMonths() {
        return months;
    }

    public void setMonths(Integer months) {
        this.months = months;
    }
}

