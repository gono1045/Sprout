package com.example.sprout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sprout.dao.SproutTagListDao;
import com.example.sprout.model.SproutItemTag;
import com.example.sprout.model.SproutTagList;
import com.example.sprout.security.AccessControlService;

/**
 * {@link SproutTagListServiceImpl} の単体テスト。
 * <p>
 * Spring コンテキストを使わずに DAO と AccessControlService をモック化して検証する。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class SproutTagListServiceImplTest {

  @Mock
  private SproutTagListDao tagListDao;

  @Mock
  private AccessControlService accessControlService;

  @InjectMocks
  private SproutTagListServiceImpl sut;

  private static final Long USER_ID = 1L;
  private static final Long TAG_ID = 20L;
  private static final Long ITEM_ID = 10L;
  private static final String LOGIN_ID = "testuser";

  // ===== selectAll =====

  @Test
  @DisplayName("selectAll — ログインユーザーのタグ一覧が返る")
  void selectAll_returnsTagList() {
    SproutTagList tag = new SproutTagList();
    tag.setTagId(TAG_ID);
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(tagListDao.selectAll(USER_ID)).thenReturn(List.of(tag));

    List<SproutTagList> result = sut.selectAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTagId()).isEqualTo(TAG_ID);
  }

  // ===== selectByTagId =====

  @Test
  @DisplayName("selectByTagId — 指定IDのタグが返る")
  void selectByTagId_returnsTag() {
    SproutTagList tag = new SproutTagList();
    tag.setTagId(TAG_ID);
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(tagListDao.selectByTagId(TAG_ID, USER_ID)).thenReturn(tag);

    SproutTagList result = sut.selectByTagId(TAG_ID);

    assertThat(result.getTagId()).isEqualTo(TAG_ID);
  }

  // ===== insert =====

  @Test
  @DisplayName("insert — 初回登録: tagSortOrder が 1 にセットされる")
  void insert_firstTag_sortOrderIs1() {
    SproutTagList model = new SproutTagList();
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(accessControlService.getLoginId()).thenReturn(LOGIN_ID);
    when(tagListDao.selectMaxSortOrder(USER_ID)).thenReturn(null); // 初回

    SproutTagList result = sut.insert(model);

    assertThat(result.getTagSortOrder()).isEqualTo(1);
    assertThat(result.getUserId()).isEqualTo(USER_ID);
    assertThat(result.getUpdateUser()).isEqualTo(LOGIN_ID);
    assertThat(result.getUpdateAt()).isNotNull();
    assertThat(result.getTagColor()).isNotNull();
    verify(tagListDao).insert(model);
  }

  @Test
  @DisplayName("insert — 既存タグあり: tagSortOrder が maxOrder + 1 にセットされる")
  void insert_existingTags_sortOrderIsMaxPlusOne() {
    SproutTagList model = new SproutTagList();
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(accessControlService.getLoginId()).thenReturn(LOGIN_ID);
    when(tagListDao.selectMaxSortOrder(USER_ID)).thenReturn(3); // 現在の最大順序

    sut.insert(model);

    assertThat(model.getTagSortOrder()).isEqualTo(4);
  }

  // ===== update =====

  @Test
  @DisplayName("update — userId / updateUser / updateAt がセットされ DAO が呼ばれる")
  void update_setsFieldsAndCallsDao() {
    SproutTagList model = new SproutTagList();
    model.setTagId(TAG_ID);
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(accessControlService.getLoginId()).thenReturn(LOGIN_ID);

    sut.update(model);

    assertThat(model.getUserId()).isEqualTo(USER_ID);
    assertThat(model.getUpdateUser()).isEqualTo(LOGIN_ID);
    assertThat(model.getUpdateAt()).isNotNull();
    verify(tagListDao).update(model);
  }

  // ===== delete =====

  @Test
  @DisplayName("delete — 中間テーブル削除 → タグ削除の順に DAO が呼ばれる")
  void delete_callsDaoInOrder() {
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);

    sut.delete(TAG_ID);

    verify(tagListDao).deleteByTagId(TAG_ID, USER_ID);
    verify(tagListDao).delete(TAG_ID, USER_ID);
  }

  // ===== selectTagsByItemId =====

  @Test
  @DisplayName("selectTagsByItemId — タスクIDに紐づくタグ一覧が返る")
  void selectTagsByItemId_returnsTagList() {
    SproutTagList tag = new SproutTagList();
    tag.setTagId(TAG_ID);
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(tagListDao.selectTagsByItemId(ITEM_ID, USER_ID)).thenReturn(List.of(tag));

    List<SproutTagList> result = sut.selectTagsByItemId(ITEM_ID);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTagId()).isEqualTo(TAG_ID);
  }

  // ===== selectAllItemTagIds =====

  @Test
  @DisplayName("selectAllItemTagIds — 複数タスクのタグIDマップが正しく構築される")
  void selectAllItemTagIds_buildsCorrectMap() {
    SproutItemTag tag1 = new SproutItemTag();
    tag1.setItemId(ITEM_ID);
    tag1.setTagId(1L);
    SproutItemTag tag2 = new SproutItemTag();
    tag2.setItemId(ITEM_ID);
    tag2.setTagId(2L);
    SproutItemTag tag3 = new SproutItemTag();
    tag3.setItemId(99L);
    tag3.setTagId(3L);
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(tagListDao.selectAllItemTags(USER_ID)).thenReturn(List.of(tag1, tag2, tag3));

    Map<Long, List<String>> result = sut.selectAllItemTagIds();

    assertThat(result).hasSize(2);
    assertThat(result.get(ITEM_ID)).containsExactlyInAnyOrder("1", "2");
    assertThat(result.get(99L)).containsExactly("3");
  }

  @Test
  @DisplayName("selectAllItemTagIds — タグなしのとき空マップが返る")
  void selectAllItemTagIds_noTags_returnsEmptyMap() {
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(tagListDao.selectAllItemTags(USER_ID)).thenReturn(List.of());

    Map<Long, List<String>> result = sut.selectAllItemTagIds();

    assertThat(result).isEmpty();
  }

  // ===== updateItemTags =====

  @Test
  @DisplayName("updateItemTags — タグIDリストがある場合: 削除後に各タグを挿入")
  void updateItemTags_withTags_deletesAndInsertsAll() {
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);

    sut.updateItemTags(ITEM_ID, List.of(1L, 2L, 3L));

    verify(tagListDao).deleteItemTagsByItemId(ITEM_ID, USER_ID);
    verify(tagListDao, times(3)).insertItemTag(any(SproutItemTag.class));
  }

  @Test
  @DisplayName("updateItemTags — タグIDリストが空の場合: 削除のみ実行, 挿入なし")
  void updateItemTags_emptyList_onlyDeletes() {
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);

    sut.updateItemTags(ITEM_ID, List.of());

    verify(tagListDao).deleteItemTagsByItemId(ITEM_ID, USER_ID);
    verify(tagListDao, never()).insertItemTag(any());
  }

  @Test
  @DisplayName("updateItemTags — タグIDリストがnullの場合: 削除のみ実行, 挿入なし")
  void updateItemTags_nullList_onlyDeletes() {
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);

    sut.updateItemTags(ITEM_ID, null);

    verify(tagListDao).deleteItemTagsByItemId(ITEM_ID, USER_ID);
    verify(tagListDao, never()).insertItemTag(any());
  }

  // ===== deleteItemTag =====

  @Test
  @DisplayName("deleteItemTag — DAO に正しい引数で呼ばれる")
  void deleteItemTag_callsDaoWithCorrectArgs() {
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);

    sut.deleteItemTag(ITEM_ID, TAG_ID);

    verify(tagListDao).deleteItemTag(ITEM_ID, TAG_ID, USER_ID);
  }
}
