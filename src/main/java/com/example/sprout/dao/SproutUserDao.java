package com.example.sprout.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.sprout.model.SproutUser;

@Mapper
public interface SproutUserDao {

  /**
   * ログインIDとプロバイダでユーザー取得
   * @param loginId ログインID
   * @param provider 認証プロバイダ
   * @return ユーザー情報
   */
  SproutUser findByLoginIdAndProvider(@Param("loginId") String loginId,
      @Param("provider") String provider);

  /**
   * メールアドレスとプロバイダでユーザー取得
   * @param email メールアドレス
   * @param provider 認証プロバイダ
   * @return ユーザー情報
   */
  SproutUser findByEmailAndProvider(@Param("email") String email,
      @Param("provider") String provider);

  /**
   * ユーザーIDでユーザー取得
   * @param userId ユーザーID
   * @return ユーザー情報
   */
  Optional<SproutUser> findByUserId(@Param("userId") Long userId);

  /**
   * ユーザー新規登録
   * @param user 登録ユーザー情報
   */
  void insert(SproutUser user);

  /**
   * ログインID更新
   * @param userId ユーザーID
   * @param loginId 新しいログインID
   */
  void updateLoginId(@Param("userId") Long userId, @Param("loginId") String loginId);

  /**
   * パスワード更新
   * @param userId ユーザーID
   * @param password ハッシュ化済みパスワード
   */
  void updatePassword(@Param("userId") Long userId, @Param("password") String password);

  /**
   * 論理削除（退会）
   * @param userId ユーザーID
   */
  void deactivate(@Param("userId") Long userId);
}
