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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sprout.dao.WorkLogDao;
import com.example.sprout.form.WorkLogForm;
import com.example.sprout.model.SproutTagList;
import com.example.sprout.model.SproutWorkLog;
import com.example.sprout.model.TagExpResult;
import com.example.sprout.model.WorkLogResult;
import com.example.sprout.security.AccessControlService;

/**
 * {@link WorkLogServiceImpl} の単体テスト。
 * <p>
 * Spring コンテキストを使わずに依存クラスをモック化して検証する。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class WorkLogServiceImplTest {

  @Mock
  private WorkLogDao workLogDao;

  @Mock
  private ExpCalculatorService expCalculatorService;

  @Mock
  private SproutTagListService tagListService;

  @Mock
  private AccessControlService accessControlService;

  @InjectMocks
  private WorkLogServiceImpl sut;

  private static final Long USER_ID = 1L;
  private static final Long ITEM_ID = 10L;

  // ===== save =====

  @Test
  @DisplayName("save — EXP計算・タグ分配・WorkLog保存が呼ばれ結果が返る")
  void save_normalCase_returnsWorkLogResult() {
    WorkLogForm form = buildForm(60, 3, ITEM_ID,
        "2026-10-11T09:00:00.000Z", "2026-10-11T10:00:00.000Z");

    SproutTagList tag = new SproutTagList();
    tag.setTagId(100L);

    TagExpResult tagResult = new TagExpResult();
    tagResult.setTagId(100L);
    tagResult.setExpGained(70);

    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(expCalculatorService.calcExp(60, 3)).thenReturn(70);
    when(tagListService.selectTagsByItemId(ITEM_ID)).thenReturn(List.of(tag));
    when(expCalculatorService.distributeExp(USER_ID, List.of(100L), 70))
        .thenReturn(List.of(tagResult));

    WorkLogResult result = sut.save(form);

    assertThat(result.getExpTotal()).isEqualTo(70);
    assertThat(result.getDurationMin()).isEqualTo(60);
    assertThat(result.getTagResults()).hasSize(1);
    assertThat(result.getTagResults().get(0).getTagId()).isEqualTo(100L);
  }

  @Test
  @DisplayName("save — WorkLogにフォームの値が正しくセットされてDAOが呼ばれる")
  void save_workLogFieldsAreSetCorrectly() {
    WorkLogForm form = buildForm(90, 5, ITEM_ID,
        "2026-10-11T09:00:00.000Z", "2026-10-11T10:30:00.000Z");
    form.setMemo("気づきメモ");

    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(expCalculatorService.calcExp(90, 5)).thenReturn(120);
    when(tagListService.selectTagsByItemId(ITEM_ID)).thenReturn(List.of());
    when(expCalculatorService.distributeExp(eq(USER_ID), any(), eq(120)))
        .thenReturn(List.of());

    sut.save(form);

    ArgumentCaptor<SproutWorkLog> captor = ArgumentCaptor.forClass(SproutWorkLog.class);
    verify(workLogDao).insert(captor.capture());
    SproutWorkLog saved = captor.getValue();

    assertThat(saved.getItemId()).isEqualTo(ITEM_ID);
    assertThat(saved.getUserId()).isEqualTo(USER_ID);
    assertThat(saved.getDurationMin()).isEqualTo(90);
    assertThat(saved.getSatisfaction()).isEqualTo(5);
    assertThat(saved.getMemo()).isEqualTo("気づきメモ");
    assertThat(saved.getExpTotal()).isEqualTo(120);
    assertThat(saved.getCreatedAt()).isNotNull();
  }

  @Test
  @DisplayName("save — タグが0件のとき tagResults が空リストで返る")
  void save_noTags_tagResultsIsEmpty() {
    WorkLogForm form = buildForm(30, 2, ITEM_ID,
        "2026-10-11T09:00:00.000Z", "2026-10-11T09:30:00.000Z");

    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(expCalculatorService.calcExp(30, 2)).thenReturn(35);
    when(tagListService.selectTagsByItemId(ITEM_ID)).thenReturn(List.of());
    when(expCalculatorService.distributeExp(eq(USER_ID), eq(List.of()), eq(35)))
        .thenReturn(List.of());

    WorkLogResult result = sut.save(form);

    assertThat(result.getTagResults()).isEmpty();
  }

  @Test
  @DisplayName("save — startedAt/endedAt が null のとき parseIso がフォールバックし例外が出ない")
  void save_nullTimestamps_doesNotThrow() {
    WorkLogForm form = buildForm(60, 3, ITEM_ID, null, null);

    when(accessControlService.getLoginUserId()).thenReturn(USER_ID);
    when(expCalculatorService.calcExp(60, 3)).thenReturn(70);
    when(tagListService.selectTagsByItemId(ITEM_ID)).thenReturn(List.of());
    when(expCalculatorService.distributeExp(eq(USER_ID), any(), eq(70)))
        .thenReturn(List.of());

    WorkLogResult result = sut.save(form);

    assertThat(result).isNotNull();
    verify(workLogDao).insert(any(SproutWorkLog.class));
  }

  // ===== helper =====

  private WorkLogForm buildForm(int durationMin, int satisfaction, Long itemId,
      String startedAt, String endedAt) {
    WorkLogForm form = new WorkLogForm();
    form.setItemId(itemId);
    form.setDurationMin(durationMin);
    form.setSatisfaction(satisfaction);
    form.setStartedAt(startedAt);
    form.setEndedAt(endedAt);
    return form;
  }
}
