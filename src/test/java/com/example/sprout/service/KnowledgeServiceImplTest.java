package com.example.sprout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sprout.dao.SproutKnowledgeDao;
import com.example.sprout.dao.SproutKnowledgeTagDao;
import com.example.sprout.model.SproutKnowledge;
import com.example.sprout.model.SproutKnowledgeTag;
import com.example.sprout.model.SproutTagList;
import com.example.sprout.model.TagExpResult;
import com.example.sprout.security.AccessControlService;

/**
 * {@link KnowledgeServiceImpl} の単体テスト。
 * <p>
 * Spring コンテキストを使わずに依存クラスをモック化して検証する。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class KnowledgeServiceImplTest {

  @Mock
  private SproutKnowledgeDao knowledgeDao;

  @Mock
  private SproutKnowledgeTagDao knowledgeTagDao;

  @Mock
  private AccessControlService accessControlService;

  @Mock
  private SproutTagListService tagListService;

  @Mock
  private ExpCalculatorService expCalculatorService;

  @InjectMocks
  private KnowledgeServiceImpl sut;

  private static final Long USER_ID     = 1L;
  private static final Long ITEM_ID     = 10L;
  private static final Long WORK_LOG_ID = 20L;
  private static final Long TAG_ID_1    = 100L;
  private static final Long TAG_ID_2    = 200L;

  // ===== saveFromWorkLog =====

  @Test
  @DisplayName("saveFromWorkLog — タスクEXPが未付与の場合、EXPを付与してフラグをマークする")
  void saveFromWorkLog_expNotGivenYet_givesExpAndMarksFlag() {
    SproutTagList tag = buildTagList(TAG_ID_1);
    TagExpResult expResult = buildExpResult(TAG_ID_1, 15, false);

    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(tagListService.selectTagsByItemId(ITEM_ID)).thenReturn(List.of(tag));
    when(knowledgeDao.existsTaskExpGivenByItemId(ITEM_ID, USER_ID)).thenReturn(false);
    when(expCalculatorService.distributeExp(USER_ID, List.of(TAG_ID_1), 15))
        .thenReturn(List.of(expResult));

    List<TagExpResult> results = sut.saveFromWorkLog(WORK_LOG_ID, ITEM_ID, "テストタイトル", "テスト要約");

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getExpGained()).isEqualTo(15);
    verify(knowledgeDao).insert(any(SproutKnowledge.class));
    verify(knowledgeTagDao).insertBatch(anyList());
    verify(knowledgeDao).markTaskExpGiven(any(), eq(USER_ID));
  }

  @Test
  @DisplayName("saveFromWorkLog — タスクEXPが付与済みの場合、EXPは付与されず空リストが返る")
  void saveFromWorkLog_expAlreadyGiven_returnsEmpty() {
    SproutTagList tag = buildTagList(TAG_ID_1);

    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(tagListService.selectTagsByItemId(ITEM_ID)).thenReturn(List.of(tag));
    when(knowledgeDao.existsTaskExpGivenByItemId(ITEM_ID, USER_ID)).thenReturn(true);

    List<TagExpResult> results = sut.saveFromWorkLog(WORK_LOG_ID, ITEM_ID, "タイトル", "要約");

    assertThat(results).isEmpty();
    verify(expCalculatorService, never()).distributeExp(any(), anyList(), anyInt());
    verify(knowledgeDao, never()).markTaskExpGiven(any(), any());
  }

  @Test
  @DisplayName("saveFromWorkLog — タスクにタグが紐付いていない場合、knowledgeTagDao.insertBatch は呼ばれない")
  void saveFromWorkLog_noTags_doesNotCallInsertBatch() {
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(tagListService.selectTagsByItemId(ITEM_ID)).thenReturn(List.of());
    when(knowledgeDao.existsTaskExpGivenByItemId(ITEM_ID, USER_ID)).thenReturn(false);

    List<TagExpResult> results = sut.saveFromWorkLog(WORK_LOG_ID, ITEM_ID, null, "要約のみ");

    assertThat(results).isEmpty();
    verify(knowledgeTagDao, never()).insertBatch(anyList());
  }

  // ===== update =====

  @Test
  @DisplayName("update — 同日に編集EXP未付与の場合、EXPを付与して last_edited_exp_at を更新する")
  void update_expNotGivenToday_givesExpAndUpdatesDate() {
    Long knowledgeId = 5L;
    SproutKnowledge knowledge = buildKnowledge(knowledgeId, LocalDate.now().minusDays(1));
    SproutKnowledgeTag knTag = buildKnowledgeTag(knowledgeId, TAG_ID_1);
    TagExpResult expResult = buildExpResult(TAG_ID_1, 8, false);

    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(knowledgeDao.selectById(knowledgeId, USER_ID)).thenReturn(knowledge);
    when(knowledgeTagDao.selectByKnowledgeId(knowledgeId, USER_ID)).thenReturn(List.of(knTag));
    when(expCalculatorService.distributeExp(USER_ID, List.of(TAG_ID_1), 8))
        .thenReturn(List.of(expResult));

    List<TagExpResult> results = sut.update(knowledgeId, "新タイトル", "新要約");

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getExpGained()).isEqualTo(8);
    verify(knowledgeDao).update(knowledgeId, "新タイトル", "新要約", USER_ID);
    verify(knowledgeDao).updateLastEditedExpAt(eq(knowledgeId), eq(LocalDate.now()), eq(USER_ID));
  }

  @Test
  @DisplayName("update — 同日に編集EXP付与済みの場合、EXPは付与されず空リストが返る")
  void update_expAlreadyGivenToday_returnsEmpty() {
    Long knowledgeId = 5L;
    SproutKnowledge knowledge = buildKnowledge(knowledgeId, LocalDate.now());

    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(knowledgeDao.selectById(knowledgeId, USER_ID)).thenReturn(knowledge);

    List<TagExpResult> results = sut.update(knowledgeId, "タイトル", "要約");

    assertThat(results).isEmpty();
    verify(expCalculatorService, never()).distributeExp(any(), anyList(), anyInt());
    verify(knowledgeDao, never()).updateLastEditedExpAt(any(), any(), any());
  }

  @Test
  @DisplayName("update — タグが空の場合、EXPは付与されず空リストが返る")
  void update_noTags_returnsEmpty() {
    Long knowledgeId = 5L;
    SproutKnowledge knowledge = buildKnowledge(knowledgeId, LocalDate.now().minusDays(1));

    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(knowledgeDao.selectById(knowledgeId, USER_ID)).thenReturn(knowledge);
    when(knowledgeTagDao.selectByKnowledgeId(knowledgeId, USER_ID)).thenReturn(List.of());

    List<TagExpResult> results = sut.update(knowledgeId, "タイトル", "要約");

    assertThat(results).isEmpty();
    verify(expCalculatorService, never()).distributeExp(any(), anyList(), anyInt());
  }

  // ===== merge =====

  @Test
  @DisplayName("merge — 2件のIDを渡すと新しい実が登録され元の実が削除される")
  void merge_twoIds_insertsNewAndDeletesOld() {
    List<Long> ids = List.of(1L, 2L);
    SproutKnowledgeTag tag1 = buildKnowledgeTag(1L, TAG_ID_1);
    SproutKnowledgeTag tag2 = buildKnowledgeTag(2L, TAG_ID_2);
    TagExpResult expResult = buildExpResult(TAG_ID_1, 25, false);

    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(knowledgeTagDao.selectByKnowledgeId(1L, USER_ID)).thenReturn(List.of(tag1));
    when(knowledgeTagDao.selectByKnowledgeId(2L, USER_ID)).thenReturn(List.of(tag2));
    when(expCalculatorService.distributeExp(eq(USER_ID), anyList(), eq(25)))
        .thenReturn(List.of(expResult));

    List<TagExpResult> results = sut.merge(ids, "マージ後タイトル", "マージ後要約");

    assertThat(results).hasSize(1);
    verify(knowledgeDao).insert(any(SproutKnowledge.class));
    verify(knowledgeTagDao).insertBatch(anyList());
    verify(knowledgeTagDao).deleteByKnowledgeIds(ids, USER_ID);
    verify(knowledgeDao).deleteByIds(ids, USER_ID);
  }

  @Test
  @DisplayName("merge — タグが重複する場合、重複排除された一意のタグセットでEXPが付与される")
  void merge_duplicateTags_deduplicatesBeforeExp() {
    List<Long> ids = List.of(1L, 2L);
    // 両方の実に同じタグ TAG_ID_1 が付いている
    SproutKnowledgeTag tag1 = buildKnowledgeTag(1L, TAG_ID_1);
    SproutKnowledgeTag tag2 = buildKnowledgeTag(2L, TAG_ID_1);
    TagExpResult expResult = buildExpResult(TAG_ID_1, 25, false);

    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(knowledgeTagDao.selectByKnowledgeId(1L, USER_ID)).thenReturn(List.of(tag1));
    when(knowledgeTagDao.selectByKnowledgeId(2L, USER_ID)).thenReturn(List.of(tag2));
    when(expCalculatorService.distributeExp(eq(USER_ID), eq(List.of(TAG_ID_1)), eq(25)))
        .thenReturn(List.of(expResult));

    sut.merge(ids, "タイトル", "要約");

    // distributeExp に渡されるタグIDが重複排除されていること
    ArgumentCaptor<List<Long>> tagCaptor = ArgumentCaptor.forClass(List.class);
    verify(expCalculatorService).distributeExp(eq(USER_ID), tagCaptor.capture(), eq(25));
    assertThat(tagCaptor.getValue()).containsExactly(TAG_ID_1);
  }

  @Test
  @DisplayName("merge — タグが0件の場合、EXPは付与されず空リストが返る")
  void merge_noTags_noExpGiven() {
    List<Long> ids = List.of(1L, 2L);

    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(knowledgeTagDao.selectByKnowledgeId(1L, USER_ID)).thenReturn(List.of());
    when(knowledgeTagDao.selectByKnowledgeId(2L, USER_ID)).thenReturn(List.of());

    List<TagExpResult> results = sut.merge(ids, "タイトル", "要約");

    assertThat(results).isEmpty();
    verify(expCalculatorService, never()).distributeExp(any(), anyList(), anyInt());
    verify(knowledgeDao).deleteByIds(ids, USER_ID);
  }

  // ===== deleteAll =====

  @Test
  @DisplayName("deleteAll — 渡したIDリストで中間テーブルと本体が削除される")
  void deleteAll_givenIds_deletesBothTables() {
    List<Long> ids = List.of(1L, 2L, 3L);
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);

    sut.deleteAll(ids);

    verify(knowledgeTagDao).deleteByKnowledgeIds(ids, USER_ID);
    verify(knowledgeDao).deleteByIds(ids, USER_ID);
  }

  @Test
  @DisplayName("deleteAll — 中間テーブルが本体より先に削除される順序を確認する")
  void deleteAll_deleteOrderIsTagsThenKnowledge() {
    List<Long> ids = List.of(5L);
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);

    sut.deleteAll(ids);

    // 順序検証: tagDao が先、knowledgeDao が後
    org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(knowledgeTagDao, knowledgeDao);
    inOrder.verify(knowledgeTagDao).deleteByKnowledgeIds(ids, USER_ID);
    inOrder.verify(knowledgeDao).deleteByIds(ids, USER_ID);
  }

  @Test
  @DisplayName("deleteAll — 空リストを渡しても例外が発生しない")
  void deleteAll_emptyList_doesNotThrow() {
    List<Long> ids = List.of();
    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);

    sut.deleteAll(ids);

    verify(knowledgeTagDao).deleteByKnowledgeIds(ids, USER_ID);
    verify(knowledgeDao).deleteByIds(ids, USER_ID);
  }

  // ===== helpers =====

  private SproutTagList buildTagList(Long tagId) {
    SproutTagList tag = new SproutTagList();
    tag.setTagId(tagId);
    return tag;
  }

  private SproutKnowledgeTag buildKnowledgeTag(Long knowledgeId, Long tagId) {
    SproutKnowledgeTag tag = new SproutKnowledgeTag();
    tag.setKnowledgeId(knowledgeId);
    tag.setTagId(tagId);
    tag.setUserId(USER_ID);
    return tag;
  }

  private SproutKnowledge buildKnowledge(Long id, LocalDate lastEditedExpAt) {
    SproutKnowledge k = new SproutKnowledge();
    k.setId(id);
    k.setUserId(USER_ID);
    k.setLastEditedExpAt(lastEditedExpAt);
    k.setCreatedAt(LocalDateTime.now());
    return k;
  }

  private TagExpResult buildExpResult(Long tagId, int expGained, boolean leveledUp) {
    TagExpResult r = new TagExpResult();
    r.setTagId(tagId);
    r.setExpGained(expGained);
    r.setLeveledUp(leveledUp);
    return r;
  }
}
