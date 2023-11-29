package com.gdsc.mount.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {NodeNameValidator.class})
public @interface NodeNameValidation {
    String message() default "Invalid name for a file or folder.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
