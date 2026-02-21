package com.example.sprout.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.example.sprout.form.SproutUserForm;

public class PasswordMatchesValidator
    implements ConstraintValidator<PasswordMatches, SproutUserForm> {

  @Override
  public boolean isValid(SproutUserForm form,
      ConstraintValidatorContext context) {

    if (form.getPassword() == null ||
        form.getConfirmPassword() == null) {
      return true; // NotBlankに任せる
    }

    return form.getPassword().equals(form.getConfirmPassword());
  }
}
