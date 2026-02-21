package com.example.sprout.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.sprout.model.SproutUser;

/**
 * Spring Security用のUserDetails実装
 */
public class SproutUserDetails implements UserDetails {

  /** ユーザー情報 **/
  private final SproutUser user;

  /**
   * コンストラクタ
   * @param user ユーザー情報
   */
  public SproutUserDetails(SproutUser user) {
    this.user = user;
  }

  /**
   * ユーザー情報を取得する
   * @return SproutUser
   */
  public SproutUser getUser() {
    return user;
  }

  /**
   * ユーザーIDを取得する
   * @return userId
   */
  public Long getUserId() {
    return user.getUserId();
  }

  /**
   * ログインIDを取得する
   * @return loginId
   */
  public String getLoginId() {
    return user.getLoginId();
  }

  /**
   * メールアドレスを取得する
   * @return email
   */
  public String getEmail() {
    return user.getEmail();
  }

  /**
   * 認証プロバイダを取得する
   * @return provider
   */
  public String getProvider() {
    return user.getProvider();
  }

  /**
   * プロバイダユーザーIDを取得する
   * @return providerUserId
   */
  public String getProviderUserId() {
    return user.getProviderUserId();
  }

  /**
   * 権限情報を取得する
   * @return GrantedAuthorityのコレクション
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  /**
   * パスワードを取得する
   * @return password
   */
  @Override
  public String getPassword() {
    return user.getPassword();
  }

  /**
   * Spring Security用のユーザー名を取得する
   * @return loginId
   */
  @Override
  public String getUsername() {
    return user.getLoginId();
  }

  /**
   * アカウント期限切れでないか判定する
   * @return true: 有効
   */
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  /**
   * アカウントロックされていないか判定する
   * @return true: ロックされていない
   */
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  /**
   * 資格情報が期限切れでないか判定する
   * @return true: 有効
   */
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  /**
   * 有効状態か判定する
   * @return true: 有効
   */
  @Override
  public boolean isEnabled() {
    return Boolean.TRUE.equals(user.getIsActive());
  }
}
