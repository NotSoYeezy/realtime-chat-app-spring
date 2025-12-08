package com.chat.realtimechat.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidEmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        // Allow null or blank values, use @NotBlank for mandatory fields
        if (value == null || value.isBlank()) {
            return true;
        }

        return value.matches(EMAIL_REGEX);
    }
}
