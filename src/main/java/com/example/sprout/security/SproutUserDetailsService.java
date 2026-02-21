package com.example.sprout.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.sprout.dao.SproutUserDao;
import com.example.sprout.model.SproutUser;

/**
 * Spring Security用のUserDetailsService実装
 * <p>
 * loginId または email でログイン可能
 * provider = "LOCAL" ユーザーを対象
 * </p>
 */
@Service
public class SproutUserDetailsService implements UserDetailsService {

  /** ユーザDAO **/
  private final SproutUserDao userDao;

  /**
   * コンストラクタ
   * @param userDao ユーザDAO
   */
  public SproutUserDetailsService(SproutUserDao userDao) {
    this.userDao = userDao;
  }

  /**
   * ログインIDまたはメールアドレスでユーザーを取得する
   * @param input loginId または email
   * @return UserDetails
   * @throws UsernameNotFoundException ユーザーが存在しない場合
   */
  @Override
  public UserDetails loadUserByUsername(String input)
      throws UsernameNotFoundException {

    SproutUser user;

    if (input.contains("@")) {
      // email で検索（LOCALユーザー限定）
      user = userDao.findByEmailAndProvider(input, "LOCAL")
          .orElseThrow(() -> new UsernameNotFoundException("ユーザーが存在しません"));
    } else {
      // loginId で検索（LOCALユーザー限定）
      user = userDao.findByLoginIdAndProvider(input, "LOCAL")
          .orElseThrow(() -> new UsernameNotFoundException("ユーザーが存在しません"));
    }

    return new SproutUserDetails(user);
  }
}
