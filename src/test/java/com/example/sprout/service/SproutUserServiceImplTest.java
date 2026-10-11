package com.example.sprout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.sprout.dao.SproutUserDao;
import com.example.sprout.model.SproutUser;

/**
 * {@link SproutUserServiceImpl} の単体テスト。
 * <p>
 * Spring コンテキストを使わずに DAO と PasswordEncoder をモック化して検証する。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class SproutUserServiceImplTest {

  @Mock
  private SproutUserDao sproutUserDao;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private SproutUserServiceImpl sut;

  // ===== registerUser =====

  @Test
  @DisplayName("registerUser — パスワードがハッシュ化され provider=LOCAL でDAOが呼ばれる")
  void registerUser_encodesPasswordAndSetsProvider() {
    SproutUser user = new SproutUser();
    user.setPassword("rawPassword");
    when(passwordEncoder.encode("rawPassword")).thenReturn("hashedPassword");

    sut.registerUser(user);

    assertThat(user.getPassword()).isEqualTo("hashedPassword");
    assertThat(user.getProvider()).isEqualTo("LOCAL");
    verify(sproutUserDao).insert(user);
  }

  @Test
  @DisplayName("registerUser — 平文パスワードのまま登録されない（ハッシュ化済みで上書きされる）")
  void registerUser_rawPasswordIsNotPersisted() {
    SproutUser user = new SproutUser();
    user.setPassword("plain");
    when(passwordEncoder.encode("plain")).thenReturn("$2a$10$hashed");

    sut.registerUser(user);

    assertThat(user.getPassword()).isNotEqualTo("plain");
  }

  // ===== existsByLoginId =====

  @Test
  @DisplayName("existsByLoginId — DAOがtrueを返すとき trueが返る")
  void existsByLoginId_returnsTrue() {
    when(sproutUserDao.existsByLoginIdAnyStatus("existingUser")).thenReturn(true);

    boolean result = sut.existsByLoginId("existingUser");

    assertThat(result).isTrue();
    verify(sproutUserDao).existsByLoginIdAnyStatus("existingUser");
  }

  @Test
  @DisplayName("existsByLoginId — DAOがfalseを返すとき falseが返る")
  void existsByLoginId_returnsFalse() {
    when(sproutUserDao.existsByLoginIdAnyStatus("newUser")).thenReturn(false);

    boolean result = sut.existsByLoginId("newUser");

    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("existsByLoginId — 退会済みアカウントも重複として検出する（anyStatusで検索）")
  void existsByLoginId_detectsDeactivatedAccount() {
    // DAOがis_activeを問わず検索することを確認（デリゲート先のDAOメソッド名で検証）
    when(sproutUserDao.existsByLoginIdAnyStatus("deactivatedUser")).thenReturn(true);

    boolean result = sut.existsByLoginId("deactivatedUser");

    assertThat(result).isTrue();
    // existsByLoginIdAnyStatus が呼ばれていることで「全ステータス対象」であることを保証
    verify(sproutUserDao).existsByLoginIdAnyStatus("deactivatedUser");
  }

  // ===== existsByEmail =====

  @Test
  @DisplayName("existsByEmail — DAOがtrueを返すとき trueが返る")
  void existsByEmail_returnsTrue() {
    when(sproutUserDao.existsByEmailAnyStatus("test@example.com")).thenReturn(true);

    boolean result = sut.existsByEmail("test@example.com");

    assertThat(result).isTrue();
    verify(sproutUserDao).existsByEmailAnyStatus("test@example.com");
  }

  @Test
  @DisplayName("existsByEmail — DAOがfalseを返すとき falseが返る")
  void existsByEmail_returnsFalse() {
    when(sproutUserDao.existsByEmailAnyStatus("new@example.com")).thenReturn(false);

    boolean result = sut.existsByEmail("new@example.com");

    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("existsByEmail — 退会済みアカウントも重複として検出する（anyStatusで検索）")
  void existsByEmail_detectsDeactivatedAccount() {
    when(sproutUserDao.existsByEmailAnyStatus("old@example.com")).thenReturn(true);

    boolean result = sut.existsByEmail("old@example.com");

    assertThat(result).isTrue();
    verify(sproutUserDao).existsByEmailAnyStatus("old@example.com");
  }
}
