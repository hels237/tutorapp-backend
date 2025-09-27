package com.backend.tutor_app.validator;

import com.backend.tutor_app.exceptions.ObjectValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class ObjectValidator <T> {
    private  final ValidatorFactory Factory = Validation.buildDefaultValidatorFactory();
    private final Validator Validator = Factory.getValidator();

    public void validate(T objectToValidate)  {

        Set<ConstraintViolation<T>> violations = Validator.validate(objectToValidate);

        if(!violations.isEmpty()){
            Set<String> errorMessages = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
            throw  new ObjectValidationException(errorMessages,objectToValidate.getClass().getName());
        }
    }
}
