package com.example.sprout.form;

import jakarta.validation.constraints.NotBlank;

/**
 * パスワード変更フォーム
 */
public class SproutPasswordChangeForm {

  /** 現在のパスワード **/
  @NotBlank(message = "現在のパスワードを入力してください")
  private String currentPassword;

  /** 新しいパスワード **/
  @NotBlank(message = "新しいパスワードを入力してください")
  private String newPassword;

  /** 新しいパスワード（確認） **/
  @NotBlank(message = "確認用パスワードを入力してください")
  private String confirmPassword;

  public String getCurrentPassword() {
    return currentPassword;
  }

  public void setCurrentPassword(String currentPassword) {
    this.currentPassword = currentPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }
}
