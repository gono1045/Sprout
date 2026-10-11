package com.example.sprout.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import com.example.sprout.model.SproutUser;
import com.example.sprout.model.WorkLogResult;
import com.example.sprout.security.SproutUserDetails;
import com.example.sprout.service.WorkLogService;

/**
 * {@link WorkLogController} の統合テスト。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class WorkLogControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private WorkLogService workLogService;

  private SproutUserDetails mockUserDetails() {
    SproutUser user = new SproutUser();
    user.setUserId(1L);
    user.setLoginId("testuser");
    user.setPassword("hashed");
    user.setIsActive(true);
    return new SproutUserDetails(user);
  }

  @Test
  @DisplayName("POST /work-log/save (認証済み) — 200 OK で WorkLogResult が返る")
  void save_authenticated_returnsWorkLogResult() throws Exception {
    WorkLogResult result = new WorkLogResult();
    result.setExpTotal(70);
    result.setDurationMin(60);
    result.setTagResults(List.of());
    when(workLogService.save(any())).thenReturn(result);

    mockMvc.perform(post("/work-log/save")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"itemId\":1,\"durationMin\":60,\"satisfaction\":3,"
                + "\"startedAt\":\"2026-10-11T09:00:00.000Z\","
                + "\"endedAt\":\"2026-10-11T10:00:00.000Z\",\"memo\":\"\"}")
            .with(user(mockUserDetails())))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.expTotal").value(70))
        .andExpect(jsonPath("$.durationMin").value(60));
  }

  @Test
  @DisplayName("POST /work-log/save (未認証) — リダイレクト")
  void save_unauthenticated_redirects() throws Exception {
    mockMvc.perform(post("/work-log/save")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().is3xxRedirection());
  }
}
