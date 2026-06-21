package com.example.sprout.service;

import com.example.sprout.model.SproutUser;

/**
 * アカウント管理サービスインターフェース
 */
public interface SproutAccountService {

  /**
   * ユーザーIDでユーザー取得
   * @param userId ユーザーID
   * @return ユーザー情報
   */
  SproutUser findByUserId(Long userId);

  /**
   * ログインID更新
   * 変更後はSecurityContextを再設定する
   * @param userId      ユーザーID
   * @param newLoginId  新しいログインID
   */
  void updateLoginId(Long userId, String newLoginId);

  /**
   * パスワード変更
   * 現在のパスワードを照合してから更新する
   * @param userId          ユーザーID
   * @param currentPassword 現在のパスワード（平文）
   * @param newPassword     新しいパスワード（平文）
   * @return 照合成功の場合 true
   */
  boolean updatePassword(Long userId, String currentPassword, String newPassword);

  /**
   * 論理削除（退会）
   * パスワードを照合してから is_active = false に更新する
   * @param userId   ユーザーID
   * @param password 確認用パスワード（平文）
   * @return 照合成功の場合 true
   */
  boolean deactivate(Long userId, String password);
}
