package com.example.sprout.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sprout.dao.WorkLogDao;
import com.example.sprout.form.WorkLogForm;
import com.example.sprout.model.AiSummaryResult;
import com.example.sprout.model.SproutWorkLog;
import com.example.sprout.model.TagExpResult;
import com.example.sprout.model.WorkLogResult;
import com.example.sprout.security.AccessControlService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 工数記録サービス実装
 */
@Service
public class WorkLogServiceImpl implements WorkLogService {

  private static final Logger logger = LoggerFactory.getLogger(WorkLogServiceImpl.class);

  private static final DateTimeFormatter ISO_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
          .withZone(java.time.ZoneOffset.UTC);

  @Autowired
  private WorkLogDao workLogDao;

  @Autowired
  private ExpCalculatorService expCalculatorService;

  @Autowired
  private SproutTagListService tagListService;

  @Autowired
  private AccessControlService accessControlService;

  @Autowired
  private AiSummaryService aiSummaryService;

  @Override
  @Transactional
  public WorkLogResult save(WorkLogForm form) {
    Long userId = accessControlService.getLoginUserId();

    // EXP 計算
    int totalExp = expCalculatorService.calcExp(form.getDurationMin(), form.getSatisfaction());

    // 対象タスクに紐づくタグ ID を取得
    List<Long> tagIds = tagListService.selectTagsByItemId(form.getItemId())
        .stream()
        .map(t -> t.getTagId())
        .collect(Collectors.toList());

    // タグへ EXP 分配（結果を受け取る）
    List<TagExpResult> tagResults = expCalculatorService.distributeExp(userId, tagIds, totalExp);

    // ワークログ保存
    SproutWorkLog log = new SproutWorkLog();
    log.setItemId(form.getItemId());
    log.setUserId(userId);
    log.setStartedAt(parseIso(form.getStartedAt()));
    log.setEndedAt(parseIso(form.getEndedAt()));
    log.setDurationMin(form.getDurationMin());
    log.setSatisfaction(form.getSatisfaction());
    log.setMemo(form.getMemo());
    log.setExpTotal(totalExp);
    log.setCreatedAt(LocalDateTime.now());

    workLogDao.insert(log);

    // AI要約生成（memoが空でない場合のみ）
    String aiTitle        = null;
    String aiSummary      = null;
    boolean aiSummaryFailed = false;
    String memo = form.getMemo();
    if (memo != null && !memo.isBlank()) {
      try {
        AiSummaryResult aiResult = aiSummaryService.summarize(memo);
        if (aiResult != null) {
          aiTitle   = aiResult.getTitle();
          aiSummary = aiResult.getSummary();
          // 空文字はnullに統一（スキップ扱い）
          if (aiSummary != null && aiSummary.isBlank()) aiSummary = null;
          if (aiTitle   != null && aiTitle.isBlank())   aiTitle   = null;
          // AiSummaryService 内部でフォールバックした場合（LM Studio 未起動等）
          if (aiResult.isFallback()) {
            aiSummaryFailed = true;
          }
        }
      } catch (Exception e) {
        logger.warn("AI要約生成に失敗しました（フォールバック: スキップ）", e);
        aiSummaryFailed = true;
      }
    }

    // レスポンス構築
    WorkLogResult result = new WorkLogResult();
    result.setExpTotal(totalExp);
    result.setDurationMin(form.getDurationMin());
    result.setTagResults(tagResults);
    result.setWorkLogId(log.getId());
    result.setItemId(form.getItemId());
    result.setAiTitle(aiTitle);
    result.setAiSummary(aiSummary);
    result.setAiSummaryFailed(aiSummaryFailed);
    return result;
  }

  // ===== private =====

  private LocalDateTime parseIso(String iso) {
    if (iso == null || iso.isBlank()) return LocalDateTime.now();
    try {
      return LocalDateTime.parse(iso, ISO_FORMATTER);
    } catch (Exception e) {
      // フォールバック: 末尾 Z なし
      return LocalDateTime.parse(iso.replace("Z", ""),
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }
  }
}
