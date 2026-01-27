package com.study.myspringstudydiary.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for Enum types
 *
 * Usage:
 * @ValidEnum(enumClass = Category.class, message = "유효하지 않은 카테고리입니다")
 * private String category;
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidator.class)
@Documented
public @interface ValidEnum {

    /**
     * The enum class to validate against
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * Error message when validation fails
     */
    String message() default "유효하지 않은 값입니다";

    /**
     * Validation groups (for advanced use cases)
     */
    Class<?>[] groups() default {};

    /**
     * Additional payload (for advanced use cases)
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Allow null values (default: true)
     */
    boolean nullable() default true;
}
