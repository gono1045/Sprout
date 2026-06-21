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
import com.example.sprout.model.SproutWorkLog;
import com.example.sprout.model.TagExpResult;
import com.example.sprout.model.WorkLogResult;
import com.example.sprout.security.AccessControlService;

/**
 * 工数記録サービス実装
 */
@Service
public class WorkLogServiceImpl implements WorkLogService {

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

    // レスポンス構築
    WorkLogResult result = new WorkLogResult();
    result.setExpTotal(totalExp);
    result.setDurationMin(form.getDurationMin());
    result.setTagResults(tagResults);
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
