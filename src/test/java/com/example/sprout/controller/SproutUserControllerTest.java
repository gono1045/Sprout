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

import com.example.sprout.service.SproutUserService;

/**
 * {@link SproutUserController} の統合テスト。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class SproutUserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SproutUserService sproutUserService;

  @Test
  @DisplayName("GET /register — 200 OK で登録画面が返る")
  void getRegister_returns200() throws Exception {
    mockMvc.perform(get("/register"))
        .andExpect(status().isOk())
        .andExpect(view().name("register"));
  }

  @Test
  @DisplayName("POST /register — バリデーションエラー時に登録画面が返る（パスワード不一致）")
  void postRegister_passwordMismatch_returnsRegisterView() throws Exception {
    mockMvc.perform(post("/register")
            .param("loginId", "newuser")
            .param("email", "new@example.com")
            .param("password", "password123")
            .param("confirmPassword", "different"))
        .andExpect(status().isOk())
        .andExpect(view().name("register"));
  }

  @Test
  @DisplayName("POST /register — loginId重複時にバリデーションエラーで登録画面が返る")
  void postRegister_loginIdDuplicate_returnsRegisterView() throws Exception {
    when(sproutUserService.existsByLoginId("existingUser")).thenReturn(true);
    when(sproutUserService.existsByEmail("new@example.com")).thenReturn(false);

    mockMvc.perform(post("/register")
            .param("loginId", "existingUser")
            .param("email", "new@example.com")
            .param("password", "password123")
            .param("confirmPassword", "password123"))
        .andExpect(status().isOk())
        .andExpect(view().name("register"))
        .andExpect(model().attributeHasFieldErrors("sproutUserForm", "loginId"));
  }

  @Test
  @DisplayName("POST /register — email重複時にバリデーションエラーで登録画面が返る")
  void postRegister_emailDuplicate_returnsRegisterView() throws Exception {
    when(sproutUserService.existsByLoginId("newuser")).thenReturn(false);
    when(sproutUserService.existsByEmail("dup@example.com")).thenReturn(true);

    mockMvc.perform(post("/register")
            .param("loginId", "newuser")
            .param("email", "dup@example.com")
            .param("password", "password123")
            .param("confirmPassword", "password123"))
        .andExpect(status().isOk())
        .andExpect(view().name("register"))
        .andExpect(model().attributeHasFieldErrors("sproutUserForm", "email"));
  }
}
