package javax.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.meta.TypeQualifier;
import javax.annotation.meta.TypeQualifierValidator;
import javax.annotation.meta.When;

@TypeQualifier(applicableTo = Number.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
/* loaded from: classes11.dex */
public @interface Nonnegative {
    When when() default When.ALWAYS;

    /* loaded from: classes11.dex */
    public static class Checker implements TypeQualifierValidator<Nonnegative> {
        @Override // javax.annotation.meta.TypeQualifierValidator
        public When forConstantValue(Nonnegative annotation, Object v) {
            boolean isNegative;
            if (!(v instanceof Number)) {
                return When.NEVER;
            }
            Number value = (Number) v;
            if (value instanceof Long) {
                isNegative = value.longValue() < 0;
            } else {
                boolean isNegative2 = value instanceof Double;
                if (isNegative2) {
                    isNegative = value.doubleValue() < 0.0d;
                } else {
                    boolean isNegative3 = value instanceof Float;
                    if (isNegative3) {
                        isNegative = value.floatValue() < 0.0f;
                    } else {
                        isNegative = value.intValue() < 0;
                    }
                }
            }
            if (isNegative) {
                return When.NEVER;
            }
            return When.ALWAYS;
        }
    }
}
