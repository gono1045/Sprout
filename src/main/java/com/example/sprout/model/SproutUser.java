package com.example.sprout.model;

import java.time.LocalDateTime;

/**
 * ユーザー管理用のModel
 */
public class SproutUser {

  /** ユーザーID **/
  private Long userId;

  /** ログインID **/
  private String loginId;

  /** メールアドレス **/
  private String email;

  /** パスワード **/
  private String password;

  /** 認証プロバイダ **/
  private String provider = "LOCAL";

  /** プロバイダユーザーID **/
  private String providerUserId;

  /** 有効状態 **/
  private Boolean isActive = true;

  /** 作成日時 **/
  private LocalDateTime createdAt;

  /** 更新日時 **/
  private LocalDateTime updatedAt;

  /**
   * ユーザーIDを取得する
   * @return userId
   */
  public Long getUserId() {
    return userId;
  }

  /**
   * ユーザーIDを設定する
   * @param userId ユーザーID
   */
  public void setUserId(Long userId) {
    this.userId = userId;
  }

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
   * 認証プロバイダを取得する
   * @return provider
   */
  public String getProvider() {
    return provider;
  }

  /**
   * 認証プロバイダを設定する
   * @param provider 認証プロバイダ
   */
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   * プロバイダユーザーIDを取得する
   * @return providerUserId
   */
  public String getProviderUserId() {
    return providerUserId;
  }

  /**
   * プロバイダユーザーIDを設定する
   * @param providerUserId プロバイダユーザーID
   */
  public void setProviderUserId(String providerUserId) {
    this.providerUserId = providerUserId;
  }

  /**
   * 有効状態を取得する
   * @return isActive
   */
  public Boolean getIsActive() {
    return isActive;
  }

  /**
   * 有効状態を設定する
   * @param isActive 有効状態
   */
  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  /**
   * 作成日時を取得する
   * @return createdAt
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * 作成日時を設定する
   * @param createdAt 作成日時
   */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * 更新日時を取得する
   * @return updatedAt
   */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  /**
   * 更新日時を設定する
   * @param updatedAt 更新日時
   */
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

}
