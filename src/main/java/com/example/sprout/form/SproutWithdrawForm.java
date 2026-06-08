package com.example.sprout.form;

import jakarta.validation.constraints.NotBlank;

/**
 * 退会確認フォーム
 */
public class SproutWithdrawForm {

  /** 確認用パスワード **/
  @NotBlank(message = "パスワードを入力してください")
  private String password;

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
