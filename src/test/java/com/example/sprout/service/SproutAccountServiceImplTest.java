package com.example.sprout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

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
 * {@link SproutAccountServiceImpl} の単体テスト。
 * <p>
 * Spring コンテキストを使わずに DAO と PasswordEncoder をモック化して検証する。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class SproutAccountServiceImplTest {

  @Mock
  private SproutUserDao sproutUserDao;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private SproutAccountServiceImpl sut;

  private static final Long USER_ID = 1L;

  // ===== findByUserId =====

  @Test
  @DisplayName("findByUserId — 存在するユーザーIDのとき対象ユーザーが返る")
  void findByUserId_exists_returnsUser() {
    SproutUser user = new SproutUser();
    user.setUserId(USER_ID);
    when(sproutUserDao.findByUserId(USER_ID)).thenReturn(Optional.of(user));

    SproutUser result = sut.findByUserId(USER_ID);

    assertThat(result.getUserId()).isEqualTo(USER_ID);
  }

  @Test
  @DisplayName("findByUserId — 存在しないユーザーIDのとき IllegalArgumentException がスローされる")
  void findByUserId_notExists_throwsIllegalArgumentException() {
    when(sproutUserDao.findByUserId(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> sut.findByUserId(99L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("99");
  }

  // ===== updateLoginId =====

  @Test
  @DisplayName("updateLoginId — DAOが正しい引数で呼ばれる")
  void updateLoginId_callsDaoWithCorrectArgs() {
    sut.updateLoginId(USER_ID, "newLoginId");

    verify(sproutUserDao).updateLoginId(USER_ID, "newLoginId");
  }

  // ===== updatePassword =====

  @Test
  @DisplayName("updatePassword — 現在パスワードが一致すれば新パスワードをハッシュ化して更新しtrueが返る")
  void updatePassword_correctCurrent_updatesAndReturnsTrue() {
    SproutUser user = new SproutUser();
    user.setUserId(USER_ID);
    user.setPassword("hashedCurrent");
    when(sproutUserDao.findByUserId(USER_ID)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("currentRaw", "hashedCurrent")).thenReturn(true);
    when(passwordEncoder.encode("newRaw")).thenReturn("hashedNew");

    boolean result = sut.updatePassword(USER_ID, "currentRaw", "newRaw");

    assertThat(result).isTrue();
    verify(sproutUserDao).updatePassword(USER_ID, "hashedNew");
  }

  @Test
  @DisplayName("updatePassword — 現在パスワードが不一致のとき更新せずfalseが返る")
  void updatePassword_incorrectCurrent_returnsFalse() {
    SproutUser user = new SproutUser();
    user.setUserId(USER_ID);
    user.setPassword("hashedCurrent");
    when(sproutUserDao.findByUserId(USER_ID)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrongRaw", "hashedCurrent")).thenReturn(false);

    boolean result = sut.updatePassword(USER_ID, "wrongRaw", "newRaw");

    assertThat(result).isFalse();
    verify(sproutUserDao, never()).updatePassword(USER_ID, "hashedNew");
  }

  // ===== deactivate =====

  @Test
  @DisplayName("deactivate — パスワードが一致すれば論理削除してtrueが返る")
  void deactivate_correctPassword_deactivatesAndReturnsTrue() {
    SproutUser user = new SproutUser();
    user.setUserId(USER_ID);
    user.setPassword("hashedPassword");
    when(sproutUserDao.findByUserId(USER_ID)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("rawPassword", "hashedPassword")).thenReturn(true);

    boolean result = sut.deactivate(USER_ID, "rawPassword");

    assertThat(result).isTrue();
    verify(sproutUserDao).deactivate(USER_ID);
  }

  @Test
  @DisplayName("deactivate — パスワードが不一致のとき論理削除せずfalseが返る")
  void deactivate_incorrectPassword_returnsFalse() {
    SproutUser user = new SproutUser();
    user.setUserId(USER_ID);
    user.setPassword("hashedPassword");
    when(sproutUserDao.findByUserId(USER_ID)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

    boolean result = sut.deactivate(USER_ID, "wrongPassword");

    assertThat(result).isFalse();
    verify(sproutUserDao, never()).deactivate(USER_ID);
  }
}
