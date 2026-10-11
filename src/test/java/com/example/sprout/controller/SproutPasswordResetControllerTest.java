package com.example.sprout.controller;

import static org.mockito.Mockito.when;
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

import com.example.sprout.service.SproutPasswordResetService;

/**
 * {@link SproutPasswordResetController} の統合テスト。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class SproutPasswordResetControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SproutPasswordResetService passwordResetService;

  @Test
  @DisplayName("GET /password-reset/request — 200 OK でリクエスト画面が返る")
  void getRequest_returns200() throws Exception {
    mockMvc.perform(get("/password-reset/request"))
        .andExpect(status().isOk())
        .andExpect(view().name("password-reset/request"));
  }

  @Test
  @DisplayName("POST /password-reset/request — バリデーションエラー時にリクエスト画面が返る")
  void postRequest_invalidEmail_returnsRequestView() throws Exception {
    mockMvc.perform(post("/password-reset/request")
            .param("email", "not-an-email"))
        .andExpect(status().isOk())
        .andExpect(view().name("password-reset/request"))
        .andExpect(model().attributeHasFieldErrors("resetRequestForm", "email"));
  }

  @Test
  @DisplayName("POST /password-reset/request — 正常送信時に /password-reset/sent にリダイレクト")
  void postRequest_validEmail_redirectsToSent() throws Exception {
    mockMvc.perform(post("/password-reset/request")
            .param("email", "user@example.com"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/password-reset/sent"));
  }

  @Test
  @DisplayName("GET /password-reset/sent — 200 OK で送信完了画面が返る")
  void getSent_returns200() throws Exception {
    mockMvc.perform(get("/password-reset/sent"))
        .andExpect(status().isOk())
        .andExpect(view().name("password-reset/sent"));
  }

  @Test
  @DisplayName("GET /password-reset/confirm — トークンが無効な場合 invalid 画面が返る")
  void getConfirm_invalidToken_returnsInvalidView() throws Exception {
    when(passwordResetService.isValidToken("bad-token")).thenReturn(false);

    mockMvc.perform(get("/password-reset/confirm").param("token", "bad-token"))
        .andExpect(status().isOk())
        .andExpect(view().name("password-reset/invalid"));
  }

  @Test
  @DisplayName("GET /password-reset/confirm — トークンが有効な場合 confirm 画面が返る")
  void getConfirm_validToken_returnsConfirmView() throws Exception {
    when(passwordResetService.isValidToken("valid-token")).thenReturn(true);

    mockMvc.perform(get("/password-reset/confirm").param("token", "valid-token"))
        .andExpect(status().isOk())
        .andExpect(view().name("password-reset/confirm"));
  }

  @Test
  @DisplayName("POST /password-reset/confirm — パスワード不一致時に confirm 画面が返る")
  void postConfirm_passwordMismatch_returnsConfirmView() throws Exception {
    mockMvc.perform(post("/password-reset/confirm")
            .param("token", "valid-token")
            .param("newPassword", "pass1")
            .param("confirmPassword", "pass2"))
        .andExpect(status().isOk())
        .andExpect(view().name("password-reset/confirm"));
  }

  @Test
  @DisplayName("POST /password-reset/confirm — トークン無効時に invalid 画面が返る")
  void postConfirm_invalidToken_returnsInvalidView() throws Exception {
    when(passwordResetService.resetPassword("bad-token", "newpass")).thenReturn(false);

    mockMvc.perform(post("/password-reset/confirm")
            .param("token", "bad-token")
            .param("newPassword", "newpass")
            .param("confirmPassword", "newpass"))
        .andExpect(status().isOk())
        .andExpect(view().name("password-reset/invalid"));
  }

  @Test
  @DisplayName("POST /password-reset/confirm — 成功時に /login にリダイレクト")
  void postConfirm_success_redirectsToLogin() throws Exception {
    when(passwordResetService.resetPassword("valid-token", "newpass")).thenReturn(true);

    mockMvc.perform(post("/password-reset/confirm")
            .param("token", "valid-token")
            .param("newPassword", "newpass")
            .param("confirmPassword", "newpass"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }
}
