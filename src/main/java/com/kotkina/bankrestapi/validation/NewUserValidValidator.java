package com.kotkina.bankrestapi.validation;

import com.kotkina.bankrestapi.web.models.requests.NewUser;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NewUserValidValidator implements ConstraintValidator<NewUserValid, NewUser> {

    @Override
    public boolean isValid(NewUser newUser, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        var constraintLogin = checkConstraint(isNotEmpty(newUser.getLogin()),
                "Login must be filled in.", context);
        var constraintPassword = checkConstraint(isNotEmpty(newUser.getPassword()),
                "Password must be filled in.", context);
        var constraintLastname = checkConstraint(isNotEmpty(newUser.getLastname()),
                "Lastname must be filled in.", context);
        var constraintFirstname = checkConstraint(isNotEmpty(newUser.getFirstname()),
                "Firstname must be filled in.", context);
        var constraintBirthdate = checkConstraint(newUser.getBirthdate() != null,
                "Birthdate must be filled in.", context);
        var constraintPhoneAndEmail = checkConstraint(isNotEmpty(newUser.getPhone()) || isNotEmpty(newUser.getEmail()),
                "At least one contact must be filled in.", context);
        var constraintDeposit = checkConstraint(newUser.getDeposit().matches("\\d+[.]?\\d{0,2}"),
                "Deposit must be a positive number with a maximum of 2 digits after the period.", context);
        return constraintLogin && constraintPassword && constraintLastname && constraintFirstname &&
                constraintBirthdate && constraintPhoneAndEmail && constraintDeposit;
    }

    private boolean isNotEmpty(String string) {
        return !(string == null || string.trim().isEmpty());
    }

    private boolean checkConstraint(Boolean constraint, String message, ConstraintValidatorContext context) {
        if (!constraint) {
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }

        return constraint;
    }
}
