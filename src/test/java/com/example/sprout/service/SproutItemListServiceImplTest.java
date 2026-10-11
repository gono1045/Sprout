package com.example.sprout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sprout.dao.SproutItemListDao;
import com.example.sprout.model.SproutItemListDetail;
import com.example.sprout.security.AccessControlService;

/**
 * {@link SproutItemListServiceImpl} の単体テスト。
 * <p>
 * Spring コンテキストを使わずに DAO と AccessControlService をモック化して検証する。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class SproutItemListServiceImplTest {

  @Mock
  private SproutItemListDao sproutItemListDao;

  @Mock
  private AccessControlService accessControlService;

  @InjectMocks
  private SproutItemListServiceImpl sut;

  private static final Long USER_ID = 1L;
  private static final Long ITEM_ID = 10L;
  private static final String LOGIN_ID = "testuser";

  // ===== init =====

  @Test
  @DisplayName("init — ログインユーザーのタスク一覧が返る")
  void init_returnsTaskList() {
    SproutItemListDetail item = new SproutItemListDetail();
    item.setId(ITEM_ID);
    item.setTitle("タスクA");
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(sproutItemListDao.selectAll(USER_ID)).thenReturn(List.of(item));

    List<SproutItemListDetail> result = sut.init();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("タスクA");
    verify(sproutItemListDao).selectAll(USER_ID);
  }

  @Test
  @DisplayName("init — タスクが0件のとき空リストが返る")
  void init_returnsEmptyList() {
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(sproutItemListDao.selectAll(USER_ID)).thenReturn(List.of());

    List<SproutItemListDetail> result = sut.init();

    assertThat(result).isEmpty();
  }

  // ===== selectByItemId =====

  @Test
  @DisplayName("selectByItemId — 指定IDのタスクが返る")
  void selectByItemId_returnsTask() {
    SproutItemListDetail item = new SproutItemListDetail();
    item.setId(ITEM_ID);
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(sproutItemListDao.selectByItemId(ITEM_ID, USER_ID)).thenReturn(item);

    SproutItemListDetail result = sut.selectByItemId(ITEM_ID);

    assertThat(result.getId()).isEqualTo(ITEM_ID);
    verify(sproutItemListDao).selectByItemId(ITEM_ID, USER_ID);
  }

  @Test
  @DisplayName("selectByItemId — 存在しないIDはnullが返る")
  void selectByItemId_returnsNull() {
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(sproutItemListDao.selectByItemId(eq(99L), eq(USER_ID))).thenReturn(null);

    SproutItemListDetail result = sut.selectByItemId(99L);

    assertThat(result).isNull();
  }

  // ===== insert =====

  @Test
  @DisplayName("insert — userId / updateUser / updateAt がセットされ DAO が呼ばれる")
  void insert_setsFieldsAndCallsDao() {
    SproutItemListDetail model = new SproutItemListDetail();
    model.setTitle("新タスク");
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(accessControlService.getLoginId()).thenReturn(LOGIN_ID);

    sut.insert(model);

    assertThat(model.getUserId()).isEqualTo(USER_ID);
    assertThat(model.getUpdateUser()).isEqualTo(LOGIN_ID);
    assertThat(model.getUpdateAt()).isNotNull();
    verify(sproutItemListDao).insert(model);
  }

  // ===== update =====

  @Test
  @DisplayName("update — userId / updateUser / updateAt がセットされ DAO が呼ばれる")
  void update_setsFieldsAndCallsDao() {
    SproutItemListDetail model = new SproutItemListDetail();
    model.setId(ITEM_ID);
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(accessControlService.getLoginId()).thenReturn(LOGIN_ID);

    sut.update(model);

    assertThat(model.getUserId()).isEqualTo(USER_ID);
    assertThat(model.getUpdateUser()).isEqualTo(LOGIN_ID);
    assertThat(model.getUpdateAt()).isNotNull();
    verify(sproutItemListDao).update(model);
  }

  // ===== delete =====

  @Test
  @DisplayName("delete — ログインユーザーIDと指定IDで DAO が呼ばれる")
  void delete_callsDaoWithUserIdAndItemId() {
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);

    sut.delete(ITEM_ID);

    verify(sproutItemListDao).delete(ITEM_ID, USER_ID);
  }
}
