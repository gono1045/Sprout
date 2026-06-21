package com.example.sprout.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.sprout.dao.SproutUserDao;
import com.example.sprout.model.SproutUser;

/**
 * ユーザー登録・確認処理サービス実装
 */
@Service
public class SproutUserServiceImpl implements SproutUserService {

  @Autowired
  private SproutUserDao sproutUserDao;

  @Autowired
  private PasswordEncoder passwordEncoder;

  /**
   * ユーザー新規登録
   * パスワードをハッシュ化して登録
   * providerは "LOCAL" をセット
   * @param user 登録ユーザー情報
   */
  @Override
  public void registerUser(SproutUser user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setProvider("LOCAL");
    sproutUserDao.insert(user);
  }

  /**
   * ログインID存在チェック（LOCALユーザー限定）
   * @param loginId ログインID
   * @return 存在する場合 true
   */
  @Override
  public boolean existsByLoginId(String loginId) {
    // DBのUNIQUE制約はis_activeを問わず適用されるため、
    // 退会済み（is_active=false）アカウントも重複として検出する
    return sproutUserDao.existsByLoginIdAnyStatus(loginId);
  }

  /**
   * メールアドレス存在チェック
   * @param email メールアドレス
   * @return 存在する場合 true
   */
  @Override
  public boolean existsByEmail(String email) {
    // DBのUNIQUE制約はis_activeを問わず適用されるため、
    // 退会済み（is_active=false）アカウントも重複として検出する
    return sproutUserDao.existsByEmailAnyStatus(email);
  }
}
