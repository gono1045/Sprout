package com.example.sprout.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.example.sprout.model.SproutTagList;
import com.example.sprout.model.SproutUser;
import com.example.sprout.security.SproutUserDetails;
import com.example.sprout.service.SproutTagListService;

/**
 * {@link SproutTagListController} の統合テスト。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class SproutTagListControllerTest {

  @Autowired
  private MockMvc mockMvc;

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
  @DisplayName("GET /tags/all (認証済み) — JSON形式のタグ一覧が返る")
  void getAllTags_authenticated_returnsJson() throws Exception {
    SproutTagList tag = new SproutTagList();
    tag.setTagId(1L);
    when(sproutTagListService.selectAll()).thenReturn(List.of(tag));

    mockMvc.perform(get("/tags/all").with(user(mockUserDetails())))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  @DisplayName("GET /tags/all (未認証) — リダイレクト")
  void getAllTags_unauthenticated_redirects() throws Exception {
    mockMvc.perform(get("/tags/all"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName("POST /tags (認証済み) — 200 OK でタグが返る")
  void createTag_authenticated_returns200() throws Exception {
    SproutTagList tag = new SproutTagList();
    tag.setTagId(1L);
    when(sproutTagListService.insert(any())).thenReturn(tag);

    mockMvc.perform(post("/tags")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"tagName\":\"テストタグ\"}")
            .with(user(mockUserDetails())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /items/{itemId}/tags (認証済み) — タスクのタグ一覧が返る")
  void getItemTags_authenticated_returnsJson() throws Exception {
    when(sproutTagListService.selectTagsByItemId(1L)).thenReturn(List.of());

    mockMvc.perform(get("/items/1/tags").with(user(mockUserDetails())))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }
}
