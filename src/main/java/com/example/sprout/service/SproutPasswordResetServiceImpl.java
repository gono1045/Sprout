package com.example.sprout.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.sprout.repository.SproutPasswordResetTokenRepository;
import com.example.sprout.dao.SproutUserDao;
import com.example.sprout.model.SproutPasswordResetToken;
import com.example.sprout.model.SproutUser;

/**
 * パスワードリセットサービス実装
 */
@Service
public class SproutPasswordResetServiceImpl implements SproutPasswordResetService {

  private static final Logger log = LoggerFactory.getLogger(SproutPasswordResetServiceImpl.class);
  private static final int TOKEN_EXPIRE_HOURS = 1;

  @Autowired
  private SproutPasswordResetTokenRepository tokenRepository;

  @Autowired
  private SproutUserDao sproutUserDao;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JavaMailSender mailSender;

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  @Value("${app.mail.from}")
  private String fromAddress;

  /**
   * {@inheritDoc}
   */
  @Override
  public void sendResetMail(String email) {
    SproutUser user = sproutUserDao.findByEmailAndProvider(email, "LOCAL");

    // ユーザーが存在しない場合も正常終了（ユーザー存在確認を防ぐため）
    if (user == null) {
      return;
    }

    // 既存の未使用トークンを無効化
    tokenRepository.invalidateAllByUserId(user.getUserId());

    // 新しいトークンを生成・保存
    String token = UUID.randomUUID().toString();
    SproutPasswordResetToken resetToken = new SproutPasswordResetToken();
    resetToken.setUserId(user.getUserId());
    resetToken.setToken(token);
    resetToken.setExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRE_HOURS));
    resetToken.setUsed(false);
    resetToken.setCreatedAt(LocalDateTime.now());
    tokenRepository.save(resetToken);

    // メール送信
    String resetUrl = baseUrl + "/password-reset/confirm?token=" + token;
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(email);
    message.setSubject("【Sprout】パスワードリセットのご案内");
    message.setText(
        user.getLoginId() + " さん\n\n" +
        "パスワードリセットのリクエストを受け付けました。\n" +
        "以下のURLからパスワードを再設定してください。\n\n" +
        resetUrl + "\n\n" +
        "このURLの有効期限は1時間です。\n" +
        "心当たりがない場合はこのメールを無視してください。\n\n" +
        "— Sprout"
    );
    try {
      mailSender.send(message);
      log.info("パスワードリセットメール送信成功: to={}", email);
    } catch (MailException e) {
      log.error("パスワードリセットメール送信失敗: to={}, error={}", email, e.getMessage(), e);
      // メール送信失敗時もトークンは保存済みのため、ユーザーフローは継続する
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValidToken(String token) {
    return tokenRepository.findByToken(token)
        .map(SproutPasswordResetToken::isValid)
        .orElse(false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean resetPassword(String token, String newPassword) {
    Optional<SproutPasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

    if (tokenOpt.isEmpty() || !tokenOpt.get().isValid()) {
      return false;
    }

    SproutPasswordResetToken resetToken = tokenOpt.get();

    // パスワード更新
    sproutUserDao.updatePassword(resetToken.getUserId(), passwordEncoder.encode(newPassword));

    // トークンを使用済みに
    resetToken.setUsed(true);
    tokenRepository.save(resetToken);

    return true;
  }
}
