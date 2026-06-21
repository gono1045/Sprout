package com.example.sprout.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import com.example.sprout.model.SproutUser;
import com.example.sprout.validation.PasswordMatches;

/**
 * ユーザー登録画面用Form
 */
@PasswordMatches
public class SproutUserForm extends SproutAbstractForm<SproutUser> {

  /** ログインID **/
  @NotBlank(message = "ログインIDを入力してください")
  private String loginId;

  /** メールアドレス **/
  @NotBlank(message = "メールアドレスを入力してください")
  @Email(message = "メールアドレスの形式が正しくありません")
  private String email;

  /** パスワード **/
  @NotBlank(message = "パスワードを入力してください")
  private String password;

  /** パスワード確認 **/
  @NotBlank(message = "確認用パスワードを入力してください")
  private String confirmPassword;

  /**
   * ログインIDを取得する
   * @return loginId
   */
  public String getLoginId() {
    return loginId;
  }

  /**
   * ログインIDを設定する
   * @param loginId ログインID
   */
  public void setLoginId(String loginId) {
    this.loginId = loginId;
  }

  /**
   * メールアドレスを取得する
   * @return email
   */
  public String getEmail() {
    return email;
  }

  /**
   * メールアドレスを設定する
   * @param email メールアドレス
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * パスワードを取得する
   * @return password
   */
  public String getPassword() {
    return password;
  }

  /**
   * パスワードを設定する
   * @param password パスワード
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * 確認用パスワードを取得する
   * @return confirmPassword
   */
  public String getConfirmPassword() {
    return confirmPassword;
  }

  /**
   * 確認用パスワードを設定する
   * @param confirmPassword 確認用パスワード
   */
  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }

  @Override
  public SproutUser createModel() {
    SproutUser model = super.createModel();
    return model;
  }

  @Override
  protected SproutUser newModel() {
    return new SproutUser();
  }
}