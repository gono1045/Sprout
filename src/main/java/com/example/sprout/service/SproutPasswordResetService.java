package com.example.sprout.service;

/**
 * パスワードリセットサービスインターフェース
 */
public interface SproutPasswordResetService {

  /**
   * パスワードリセットメールを送信する
   * メールアドレスに一致するユーザーが存在する場合のみトークンを発行して送信する
   * 存在しない場合も例外は投げない（ユーザー存在確認を防ぐため）
   * @param email 入力されたメールアドレス
   */
  void sendResetMail(String email);

  /**
   * トークンの有効性を検証する
   * @param token トークン文字列
   * @return 有効な場合 true
   */
  boolean isValidToken(String token);

  /**
   * パスワードをリセットする
   * @param token    トークン文字列
   * @param newPassword 新しいパスワード（平文）
   * @return 成功した場合 true（無効・期限切れトークンの場合 false）
   */
  boolean resetPassword(String token, String newPassword);
}
