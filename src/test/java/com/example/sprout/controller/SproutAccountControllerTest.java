package com.example.sprout.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.sprout.model.SproutUser;
import com.example.sprout.security.SproutUserDetails;
import com.example.sprout.service.SproutAccountService;
import com.example.sprout.service.SproutUserService;

/**
 * {@link SproutAccountController} の統合テスト。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class SproutAccountControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SproutAccountService sproutAccountService;

  @MockBean
  private SproutUserService sproutUserService;

  private SproutUserDetails mockUserDetails() {
    SproutUser user = new SproutUser();
    user.setUserId(1L);
    user.setLoginId("testuser");
    user.setPassword("hashed");
    user.setIsActive(true);
    return new SproutUserDetails(user);
  }

  // ===== プロフィール =====

  @Test
  @DisplayName("GET /account/profile (認証済み) — 200 OK でプロフィール画面が返る")
  void getProfile_authenticated_returns200() throws Exception {
    mockMvc.perform(get("/account/profile").with(user(mockUserDetails())))
        .andExpect(status().isOk())
        .andExpect(view().name("account/profile"));
  }

  @Test
  @DisplayName("POST /account/profile — ユーザー名が同一の場合エラーで画面が返る")
  void postProfile_sameLoginId_returnsProfileView() throws Exception {
    mockMvc.perform(post("/account/profile")
            .param("loginId", "testuser")  // 現在と同一
            .with(user(mockUserDetails())))
        .andExpect(status().isOk())
        .andExpect(view().name("account/profile"));
  }

  // ===== パスワード変更 =====

  @Test
  @DisplayName("GET /account/password (認証済み) — 200 OK でパスワード変更画面が返る")
  void getPassword_authenticated_returns200() throws Exception {
    mockMvc.perform(get("/account/password").with(user(mockUserDetails())))
        .andExpect(status().isOk())
        .andExpect(view().name("account/password"));
  }

  @Test
  @DisplayName("POST /account/password — 新パスワード不一致時にエラーで画面が返る")
  void postPassword_mismatch_returnsPasswordView() throws Exception {
    mockMvc.perform(post("/account/password")
            .param("currentPassword", "current")
            .param("newPassword", "newpass1")
            .param("confirmPassword", "newpass2")  // 不一致
            .with(user(mockUserDetails())))
        .andExpect(status().isOk())
        .andExpect(view().name("account/password"))
        .andExpect(model().attributeHasFieldErrors("sproutPasswordChangeForm", "confirmPassword"));
  }

  @Test
  @DisplayName("POST /account/password — 現在パスワード不一致時にエラーで画面が返る")
  void postPassword_wrongCurrent_returnsPasswordView() throws Exception {
    when(sproutAccountService.updatePassword(1L, "wrongpass", "newpass123")).thenReturn(false);

    mockMvc.perform(post("/account/password")
            .param("currentPassword", "wrongpass")
            .param("newPassword", "newpass123")
            .param("confirmPassword", "newpass123")
            .with(user(mockUserDetails())))
        .andExpect(status().isOk())
        .andExpect(view().name("account/password"));
  }

  @Test
  @DisplayName("POST /account/password — 成功時に /account/password にリダイレクト")
  void postPassword_success_redirects() throws Exception {
    when(sproutAccountService.updatePassword(1L, "current", "newpass123")).thenReturn(true);

    mockMvc.perform(post("/account/password")
            .param("currentPassword", "current")
            .param("newPassword", "newpass123")
            .param("confirmPassword", "newpass123")
            .with(user(mockUserDetails())))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/account/password"));
  }

  // ===== アカウント設定 =====

  @Test
  @DisplayName("GET /account/settings (認証済み) — 200 OK で設定画面が返る")
  void getSettings_authenticated_returns200() throws Exception {
    SproutUser user = new SproutUser();
    user.setUserId(1L);
    user.setEmail("test@example.com");
    when(sproutAccountService.findByUserId(1L)).thenReturn(user);

    mockMvc.perform(get("/account/settings").with(user(mockUserDetails())))
        .andExpect(status().isOk())
        .andExpect(view().name("account/settings"));
  }
}
