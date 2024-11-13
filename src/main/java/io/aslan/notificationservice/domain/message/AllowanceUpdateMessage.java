package io.aslan.notificationservice.domain.message;

import java.math.BigDecimal;

public record AllowanceUpdateMessage(
        Long id,
        String firstName,
        String lastName,
        BigDecimal currentMonthlyAllowance,
        BigDecimal newMonthlyAllowance,
        String email) {
}
