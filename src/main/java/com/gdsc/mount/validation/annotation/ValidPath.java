package com.gdsc.mount.validation.annotation;

import com.gdsc.mount.validation.implementation.PathValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {PathValidator.class})
public @interface ValidPath {
    String message() default "Invalid path.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
