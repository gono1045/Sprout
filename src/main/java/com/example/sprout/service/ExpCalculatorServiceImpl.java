package com.example.sprout.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sprout.dao.SproutTagListDao;
import com.example.sprout.enums.SproutStage;
import com.example.sprout.model.SproutTagList;
import com.example.sprout.model.TagExpResult;

/**
 * EXP計算・タグへの分配を担うサービス実装。
 * ステージ定義は {@link SproutStage} enum で管理する。
 */
@Service
public class ExpCalculatorServiceImpl implements ExpCalculatorService {

  /** 達成感ボーナステーブル（インデックス = satisfaction-1） */
  private static final int[] SATISFACTION_BONUS = { 0, 5, 10, 20, 30 };

  @Autowired
  private SproutTagListDao tagListDao;

  // ===== public API =====

  @Override
  public int calcExp(int durationMin, int satisfaction) {
    if (durationMin < 0) {
      throw new IllegalArgumentException("durationMin は 0 以上を指定してください: " + durationMin);
    }
    if (satisfaction < 1 || satisfaction > 5) {
      throw new IllegalArgumentException("satisfaction は 1〜5 を指定してください: " + satisfaction);
    }
    return durationMin + SATISFACTION_BONUS[satisfaction - 1];
  }

  @Override
  @Transactional
  public List<TagExpResult> distributeExp(Long userId, List<Long> tagIds, int totalExp) {
    List<TagExpResult> results = new ArrayList<>();
    if (tagIds == null || tagIds.isEmpty()) return results;

    int perTag = totalExp / tagIds.size();
    if (perTag <= 0) return results;
  public void distributeExp(Long userId, List<Long> tagIds, int totalExp) {
    if (tagIds == null || tagIds.isEmpty()) return;

    // 均等割り（端数切り捨て）
    int perTag = totalExp / tagIds.size();
    if (perTag <= 0) return;

    for (Long tagId : tagIds) {
      SproutTagList tag = tagListDao.selectByTagId(tagId, userId);
      if (tag == null) continue;

      int oldLv  = tag.getLv();
      int newExp = tag.getExp() + perTag;
      int newLv  = SproutStage.fromExp(newExp).getLv();
      tagListDao.updateExp(tagId, userId, newExp, newLv);

      TagExpResult r = new TagExpResult();
      r.setTagId(tagId);
      r.setTagName(tag.getTagName());
      r.setTagColor(tag.getTagColor());
      r.setExpGained(perTag);
      r.setNewExp(newExp);
      r.setNewLv(newLv);
      r.setLeveledUp(newLv > oldLv);
      results.add(r);
    }
    return results;
      int newExp = tag.getExp() + perTag;
      int newLv  = calcLv(newExp);
      tagListDao.updateExp(tagId, userId, newExp, newLv);
    }
  }

  // ===== package-private for test =====

  static int calcLv(int totalExp) {
    return SproutStage.fromExp(totalExp).getLv();
  }
}
