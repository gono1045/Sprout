package com.example.sprout.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.sprout.model.SproutItemListDetail;
import com.example.sprout.model.SproutUser;
import com.example.sprout.security.SproutUserDetails;
import com.example.sprout.service.SproutItemListService;
import com.example.sprout.service.SproutTagListService;

/**
 * {@link SproutItemListController} の統合テスト。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class SproutItemListControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SproutItemListService sproutItemListService;

  @MockBean
  private SproutTagListService sproutTagListService;

  private SproutUserDetails mockUserDetails() {
    SproutUser user = new SproutUser();
    user.setUserId(1L);
    user.setLoginId("testuser");
    user.setPassword("hashed");
    user.setIsActive(true);
    return new SproutUserDetails(user);
  }

  @Test
  @DisplayName("GET / (未認証) — ログイン画面にリダイレクト")
  void getRoot_unauthenticated_redirectsToLogin() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName("GET / (認証済み) — 200 OK でTop画面が返る")
  void getRoot_authenticated_returns200() throws Exception {
    when(sproutItemListService.init()).thenReturn(List.of());

    mockMvc.perform(get("/").with(user(mockUserDetails())))
        .andExpect(status().isOk())
        .andExpect(view().name("sproutTop"));
  }

  @Test
  @DisplayName("GET /task/list (認証済み) — JSON形式のタスク一覧が返る")
  void getTaskList_authenticated_returnsJson() throws Exception {
    SproutItemListDetail item = new SproutItemListDetail();
    item.setId(1L);
    item.setTitle("テストタスク");
    when(sproutItemListService.init()).thenReturn(List.of(item));

    mockMvc.perform(get("/task/list").with(user(mockUserDetails())))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  @DisplayName("POST /task/delete (認証済み) — 200 OK")
  void deleteTask_authenticated_returns200() throws Exception {
    mockMvc.perform(post("/task/delete")
            .param("id", "1")
            .with(user(mockUserDetails())))
        .andExpect(status().isOk());
  }
}
