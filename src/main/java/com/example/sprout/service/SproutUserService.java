package com.example.sprout.service;

import com.example.sprout.model.SproutUser;

/**
 * ユーザー登録・確認サービスインターフェース
 * LOCALユーザーを対象とする
 */
public interface SproutUserService {

  /**
   * ユーザー新規登録
   * パスワードはハッシュ化され、providerは "LOCAL" として登録される
   * @param user 登録ユーザー情報
   */
  void registerUser(SproutUser user);

  /**
   * ログインID存在チェック（LOCALユーザー限定）
   * @param loginId ログインID
   * @return 存在する場合true
   */
  boolean existsByLoginId(String loginId);

  /**
   * メールアドレス存在チェック（LOCALユーザー限定）
   * @param email メールアドレス
   * @return 存在する場合true
   */
  boolean existsByEmail(String email);
}