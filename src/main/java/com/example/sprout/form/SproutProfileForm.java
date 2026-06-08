package com.example.sprout.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * プロフィール変更フォーム（ユーザー名変更）
 */
public class SproutProfileForm {

  /** 新しいログインID **/
  @NotBlank(message = "ユーザー名を入力してください")
  @Size(max = 50, message = "ユーザー名は50文字以内で入力してください")
  private String loginId;

  public String getLoginId() {
    return loginId;
  }

  public void setLoginId(String loginId) {
    this.loginId = loginId;
  }
}
