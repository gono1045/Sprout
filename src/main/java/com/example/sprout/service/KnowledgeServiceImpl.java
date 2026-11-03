package com.example.sprout.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sprout.dao.SproutKnowledgeDao;
import com.example.sprout.dao.SproutKnowledgeTagDao;
import com.example.sprout.model.KnowledgeListItem;
import com.example.sprout.model.SproutKnowledge;
import com.example.sprout.model.SproutKnowledgeTag;
import com.example.sprout.model.TagExpResult;
import com.example.sprout.security.AccessControlService;
import com.example.sprout.service.ExpCalculatorService;

/**
 * {@link KnowledgeService} 実装
 */
@Service
public class KnowledgeServiceImpl implements KnowledgeService {

  @Autowired
  private SproutKnowledgeDao knowledgeDao;

  @Autowired
  private SproutKnowledgeTagDao knowledgeTagDao;

  @Autowired
  private AccessControlService accessControlService;

  @Autowired
  private SproutTagListService tagListService;

  @Autowired
  private ExpCalculatorService expCalculatorService;

  /** 保存EXP（同一タスク1回限り） */
  private static final int EXP_SAVE   = 15;
  /** 編集EXP（同日1回） */
  private static final int EXP_EDIT   = 8;
  /** マージEXP */
  private static final int EXP_MERGE  = 25;

  @Override
  public List<KnowledgeListItem> getList(String sortKey) {
    Long userId = accessControlService.getLoginUserId();
    String sort = (sortKey == null || sortKey.isBlank()) ? "new" : sortKey;
    return knowledgeDao.selectListByUserId(userId, sort);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    Long userId = accessControlService.getLoginUserId();
    // 中間テーブルを先に削除してから本体を削除
    knowledgeTagDao.deleteByKnowledgeId(id, userId);
    knowledgeDao.deleteById(id, userId);
  }

  @Override
  @Transactional
  public List<TagExpResult> saveFromWorkLog(Long workLogId, Long itemId, String title, String aiSummary) {
    Long userId = accessControlService.getLoginUserId();

    // 木の実本体を登録
    SproutKnowledge knowledge = new SproutKnowledge();
    knowledge.setUserId(userId);
    knowledge.setWorkLogId(workLogId);
    knowledge.setItemId(itemId);
    knowledge.setTitle(title);
    knowledge.setAiSummary(aiSummary);
    knowledge.setCreatedAt(LocalDateTime.now());
    knowledgeDao.insert(knowledge);

    // タスクに紐づくタグを継承して中間テーブルに登録
    List<Long> tagIds = tagListService.selectTagsByItemId(itemId)
        .stream()
        .map(t -> t.getTagId())
        .collect(Collectors.toList());
    if (!tagIds.isEmpty()) {
      Long knowledgeId = knowledge.getId();
      List<SproutKnowledgeTag> tags = tagIds.stream().map(tagId -> {
        SproutKnowledgeTag tag = new SproutKnowledgeTag();
        tag.setKnowledgeId(knowledgeId);
        tag.setTagId(tagId);
        tag.setUserId(userId);
        return tag;
      }).collect(Collectors.toList());
      knowledgeTagDao.insertBatch(tags);
    }

    // 同一タスクへの保存EXPが未付与の場合のみ付与する
    List<TagExpResult> expResults = Collections.emptyList();
    try {
      if (itemId != null && !knowledgeDao.existsTaskExpGivenByItemId(itemId, userId)) {
        expResults = expCalculatorService.distributeExp(userId, tagIds, EXP_SAVE);
        knowledgeDao.markTaskExpGiven(knowledge.getId(), userId);
      }
    } catch (Exception e) {
      // EXP付与に失敗しても保存自体は成功とする
      org.slf4j.LoggerFactory.getLogger(getClass()).error("保存EXP付与に失敗しました itemId={}", itemId, e);
    }
    return expResults;
  }

  @Override
  @Transactional
  public List<TagExpResult> update(Long id, String title, String aiSummary) {
    Long userId = accessControlService.getLoginUserId();
    knowledgeDao.update(id, title, aiSummary, userId);

    // 同日に編集EXPが未付与の場合のみ付与する
    try {
      SproutKnowledge knowledge = knowledgeDao.selectById(id, userId);
      if (knowledge != null) {
        LocalDate today = LocalDate.now();
        if (!today.equals(knowledge.getLastEditedExpAt())) {
          List<Long> tagIds = knowledgeTagDao.selectByKnowledgeId(id, userId)
              .stream().map(SproutKnowledgeTag::getTagId).collect(Collectors.toList());
          if (!tagIds.isEmpty()) {
            List<TagExpResult> expResults = expCalculatorService.distributeExp(userId, tagIds, EXP_EDIT);
            knowledgeDao.updateLastEditedExpAt(id, today, userId);
            return expResults;
          }
        }
      }
    } catch (Exception e) {
      // EXP付与に失敗しても更新自体は成功とする
      org.slf4j.LoggerFactory.getLogger(getClass()).error("編集EXP付与に失敗しました id={}", id, e);
    }
    return Collections.emptyList();
  }

  @Override
  @Transactional
  public List<TagExpResult> merge(List<Long> ids, String title, String aiSummary) {
    Long userId = accessControlService.getLoginUserId();

    // 対象の実が持つタグIDを重複なく収集
    Set<Long> tagIdSet = new LinkedHashSet<>();
    for (Long id : ids) {
      List<SproutKnowledgeTag> tags = knowledgeTagDao.selectByKnowledgeId(id, userId);
      tags.stream().map(SproutKnowledgeTag::getTagId).forEach(tagIdSet::add);
    }

    // 新しい実を登録（itemId / workLogId は null）
    SproutKnowledge merged = new SproutKnowledge();
    merged.setUserId(userId);
    merged.setTitle(title);
    merged.setAiSummary(aiSummary);
    merged.setCreatedAt(LocalDateTime.now());
    knowledgeDao.insert(merged);

    // 合算タグを新しい実に紐付け
    if (!tagIdSet.isEmpty()) {
      Long newId = merged.getId();
      List<SproutKnowledgeTag> newTags = new ArrayList<>();
      for (Long tagId : tagIdSet) {
        SproutKnowledgeTag tag = new SproutKnowledgeTag();
        tag.setKnowledgeId(newId);
        tag.setTagId(tagId);
        tag.setUserId(userId);
        newTags.add(tag);
      }
      knowledgeTagDao.insertBatch(newTags);
    }

    // 元の実を削除（中間テーブル → 本体の順）
    knowledgeTagDao.deleteByKnowledgeIds(ids, userId);
    knowledgeDao.deleteByIds(ids, userId);

    // マージEXPを新しい実のタグに付与する
    List<TagExpResult> expResults = Collections.emptyList();
    if (!tagIdSet.isEmpty()) {
      expResults = expCalculatorService.distributeExp(userId, new ArrayList<>(tagIdSet), EXP_MERGE);
    }
    return expResults;
  }

  @Override
  @Transactional
  public void deleteAll(List<Long> ids) {
    Long userId = accessControlService.getLoginUserId();
    knowledgeTagDao.deleteByKnowledgeIds(ids, userId);
    knowledgeDao.deleteByIds(ids, userId);
  }
}
