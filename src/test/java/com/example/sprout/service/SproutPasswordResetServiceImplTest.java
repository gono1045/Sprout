package com.example.sprout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.sprout.dao.SproutUserDao;
import com.example.sprout.model.SproutPasswordResetToken;
import com.example.sprout.model.SproutUser;
import com.example.sprout.repository.SproutPasswordResetTokenRepository;

/**
 * {@link SproutPasswordResetServiceImpl} の単体テスト。
 * <p>
 * Spring コンテキストを使わずに依存クラスをモック化して検証する。
 * {@code @Value} フィールドは {@link ReflectionTestUtils#setField} で注入する。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class SproutPasswordResetServiceImplTest {

  @Mock
  private SproutPasswordResetTokenRepository tokenRepository;

  @Mock
  private SproutUserDao sproutUserDao;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JavaMailSender mailSender;

  @InjectMocks
  private SproutPasswordResetServiceImpl sut;

  @BeforeEach
  void injectValueFields() {
    ReflectionTestUtils.setField(sut, "baseUrl", "http://localhost:8080");
    ReflectionTestUtils.setField(sut, "fromAddress", "no-reply@sprout.example.com");
  }

  // ===== sendResetMail =====

  @Test
  @DisplayName("sendResetMail — メールアドレスに一致するユーザーが存在しない場合は何もしない")
  void sendResetMail_userNotFound_doesNothing() {
    when(sproutUserDao.findByEmailAndProvider("unknown@example.com", "LOCAL")).thenReturn(null);

    sut.sendResetMail("unknown@example.com");

    verify(tokenRepository, never()).invalidateAllByUserId(any());
    verify(tokenRepository, never()).save(any());
    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("sendResetMail — ユーザーが存在する場合: 既存トークン無効化→新トークン保存→メール送信")
  void sendResetMail_userExists_invalidatesOldTokenAndSendsNewToken() {
    SproutUser user = buildUser(1L, "testuser");
    when(sproutUserDao.findByEmailAndProvider("test@example.com", "LOCAL")).thenReturn(user);

    sut.sendResetMail("test@example.com");

    verify(tokenRepository).invalidateAllByUserId(1L);

    ArgumentCaptor<SproutPasswordResetToken> tokenCaptor =
        forClass(SproutPasswordResetToken.class);
    verify(tokenRepository).save(tokenCaptor.capture());
    SproutPasswordResetToken saved = tokenCaptor.getValue();
    assertThat(saved.getUserId()).isEqualTo(1L);
    assertThat(saved.getToken()).isNotBlank();
    assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now());
    assertThat(saved.getUsed()).isFalse();
    assertThat(saved.getCreatedAt()).isNotNull();

    ArgumentCaptor<SimpleMailMessage> mailCaptor = forClass(SimpleMailMessage.class);
    verify(mailSender).send(mailCaptor.capture());
    SimpleMailMessage mail = mailCaptor.getValue();
    assertThat(mail.getTo()).containsExactly("test@example.com");
    assertThat(mail.getFrom()).isEqualTo("no-reply@sprout.example.com");
    assertThat(mail.getSubject()).contains("パスワードリセット");
    assertThat(mail.getText()).contains("http://localhost:8080/password-reset/confirm?token=");
    assertThat(mail.getText()).contains("testuser");
  }

  @Test
  @DisplayName("sendResetMail — メール送信失敗時に例外が外へ漏れない（トークンは保存済み）")
  void sendResetMail_mailSendFails_doesNotThrow() {
    SproutUser user = buildUser(2L, "user2");
    when(sproutUserDao.findByEmailAndProvider("user2@example.com", "LOCAL")).thenReturn(user);
    doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

    assertThatCode(() -> sut.sendResetMail("user2@example.com")).doesNotThrowAnyException();

    // トークンは保存済みであること
    verify(tokenRepository).save(any(SproutPasswordResetToken.class));
  }

  // ===== isValidToken =====

  @Test
  @DisplayName("isValidToken — トークンが存在しない場合は false")
  void isValidToken_tokenNotFound_returnsFalse() {
    when(tokenRepository.findByToken("no-such-token")).thenReturn(Optional.empty());

    assertThat(sut.isValidToken("no-such-token")).isFalse();
  }

  @Test
  @DisplayName("isValidToken — トークンが使用済みの場合は false")
  void isValidToken_usedToken_returnsFalse() {
    SproutPasswordResetToken token = buildToken(1L, false, true); // 期限内・使用済み
    when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(token));

    assertThat(sut.isValidToken("used-token")).isFalse();
  }

  @Test
  @DisplayName("isValidToken — トークンが期限切れの場合は false")
  void isValidToken_expiredToken_returnsFalse() {
    SproutPasswordResetToken token = buildToken(1L, true, false); // 期限切れ・未使用
    when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

    assertThat(sut.isValidToken("expired-token")).isFalse();
  }

  @Test
  @DisplayName("isValidToken — 有効なトークン（未使用かつ期限内）は true")
  void isValidToken_validToken_returnsTrue() {
    SproutPasswordResetToken token = buildToken(1L, false, false); // 期限内・未使用
    when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

    assertThat(sut.isValidToken("valid-token")).isTrue();
  }

  // ===== resetPassword =====

  @Test
  @DisplayName("resetPassword — トークンが存在しない場合は false")
  void resetPassword_tokenNotFound_returnsFalse() {
    when(tokenRepository.findByToken("no-such-token")).thenReturn(Optional.empty());

    boolean result = sut.resetPassword("no-such-token", "newPass");

    assertThat(result).isFalse();
    verify(sproutUserDao, never()).updatePassword(any(), any());
  }

  @Test
  @DisplayName("resetPassword — トークンが無効（使用済み）の場合は false")
  void resetPassword_invalidToken_returnsFalse() {
    SproutPasswordResetToken token = buildToken(1L, false, true); // 使用済み
    when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.of(token));

    boolean result = sut.resetPassword("invalid-token", "newPass");

    assertThat(result).isFalse();
    verify(sproutUserDao, never()).updatePassword(any(), any());
  }

  @Test
  @DisplayName("resetPassword — 有効なトークンの場合: パスワード更新・トークン使用済み化・true")
  void resetPassword_validToken_updatesPasswordAndMarksUsed() {
    SproutPasswordResetToken token = buildToken(1L, false, false); // 有効
    token.setToken("valid-token");
    when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
    when(passwordEncoder.encode("newRawPass")).thenReturn("hashedNewPass");

    boolean result = sut.resetPassword("valid-token", "newRawPass");

    assertThat(result).isTrue();
    verify(sproutUserDao).updatePassword(1L, "hashedNewPass");
    assertThat(token.getUsed()).isTrue();
    verify(tokenRepository).save(token);
  }

  // ===== helpers =====

  private SproutUser buildUser(Long userId, String loginId) {
    SproutUser user = new SproutUser();
    user.setUserId(userId);
    user.setLoginId(loginId);
    return user;
  }

  /**
   * @param userId     ユーザーID
   * @param expired    true = 有効期限切れ
   * @param used       true = 使用済み
   */
  private SproutPasswordResetToken buildToken(Long userId, boolean expired, boolean used) {
    SproutPasswordResetToken token = new SproutPasswordResetToken();
    token.setUserId(userId);
    token.setToken("dummy-token-" + userId);
    token.setExpiresAt(expired
        ? LocalDateTime.now().minusHours(1)   // 期限切れ
        : LocalDateTime.now().plusHours(1));  // 期限内
    token.setUsed(used);
    token.setCreatedAt(LocalDateTime.now());
    return token;
  }
}
