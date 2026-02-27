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
   * ユーザー新規登録
   * @param user 登録ユーザー情報
   */
  void insert(SproutUser user);
}
