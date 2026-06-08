package com.example.sprout.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.sprout.dao.SproutUserDao;
import com.example.sprout.model.SproutUser;

/**
 * アカウント管理サービス実装
 */
@Service
public class SproutAccountServiceImpl implements SproutAccountService {

  @Autowired
  private SproutUserDao sproutUserDao;

  @Autowired
  private PasswordEncoder passwordEncoder;

  /**
   * {@inheritDoc}
   */
  @Override
  public SproutUser findByUserId(Long userId) {
    return sproutUserDao.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません: " + userId));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateLoginId(Long userId, String newLoginId) {
    sproutUserDao.updateLoginId(userId, newLoginId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean updatePassword(Long userId, String currentPassword, String newPassword) {
    SproutUser user = findByUserId(userId);
    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      return false;
    }
    sproutUserDao.updatePassword(userId, passwordEncoder.encode(newPassword));
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean deactivate(Long userId, String password) {
    SproutUser user = findByUserId(userId);
    if (!passwordEncoder.matches(password, user.getPassword())) {
      return false;
    }
    sproutUserDao.deactivate(userId);
    return true;
  }
}
