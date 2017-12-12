package com.example.notes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Null;

import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.constraints.NotBlank;

@ConstraintComposition(CompositionType.OR)
@Constraint(validatedBy = {})
@Null
@NotBlank
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface NullOrNotBlank {

	String message() default "Must be null or not blank";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
