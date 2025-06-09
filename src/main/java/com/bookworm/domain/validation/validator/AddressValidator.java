package com.bookworm.domain.validation.validator;

import com.bookworm.domain.validation.annotation.ValidAddress;
import com.bookworm.domain.validation.utils.AddressValidationUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

import java.util.List;

/**
 * ValidAddress 어노테이션을 처리하는 검증기
 */
public class AddressValidator implements ConstraintValidator<ValidAddress, Object> {

    private String streetField;
    private String cityField;
    private String stateField;
    private String countryField;

    @Override
    public void initialize(ValidAddress constraintAnnotation) {
        this.streetField = constraintAnnotation.street();
        this.cityField = constraintAnnotation.city();
        this.stateField = constraintAnnotation.state();
        this.countryField = constraintAnnotation.country();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null 객체는 @NotNull로 처리
        }

        BeanWrapperImpl wrapper = new BeanWrapperImpl(value);

        // 필드 값 추출
        String street = (String) wrapper.getPropertyValue(streetField);
        String city = (String) wrapper.getPropertyValue(cityField);
        String state = (String) wrapper.getPropertyValue(stateField);
        String country = (String) wrapper.getPropertyValue(countryField);

        // 유틸리티 클래스를 사용하여 검증
        List<String> errors = AddressValidationUtils.validateAddress(street, city, state, country);

        if (!errors.isEmpty()) {
            context.disableDefaultConstraintViolation();
            // 각 오류에 대해 적절한 필드에 위반 사항을 추가
            errors.forEach(error -> {
                String field = null;

                if (error.contains("도로명")) {
                    field = streetField;
                } else if (error.contains("도시")) {
                    field = cityField;
                } else if (error.contains("주/도")) {
                    field = stateField;
                } else if (error.contains("국가")) {
                    field = countryField;
                }

                if (field != null) {
                    context.buildConstraintViolationWithTemplate(error)
                            .addPropertyNode(field)
                            .addConstraintViolation();
                } else {
                    // 필드를 특정할 수 없는 오류는 객체 수준 오류로 추가
                    context.buildConstraintViolationWithTemplate(error)
                            .addConstraintViolation();
                }
            });

            return false;
        }

        return true;
    }
}