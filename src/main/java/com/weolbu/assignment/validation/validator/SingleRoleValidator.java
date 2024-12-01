package com.weolbu.assignment.validation.validator;

import com.weolbu.assignment.entity.Role;
import com.weolbu.assignment.validation.annotation.SingleRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SingleRoleValidator implements ConstraintValidator<SingleRole, String> {
    @Override
    public boolean isValid(String role, ConstraintValidatorContext context){
        if (role == null)
            return false;
        try{
            Role.valueOf(role.toUpperCase());
            return true;
        } catch (IllegalArgumentException e){
            return false;
        }
    }
}
