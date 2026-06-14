package com.example.sprout.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sprout.dao.SproutTagListDao;
import com.example.sprout.model.SproutTagList;
import com.example.sprout.model.TagExpResult;

/**
 * EXP計算・タグへの分配を担うサービス実装。
 */
@Service
public class ExpCalculatorServiceImpl implements ExpCalculatorService {

  /** Lv閾値テーブル（インデックス = Lv-1, 値 = そのLvに到達するのに必要な累計EXP） */
  private static final int[] LV_THRESHOLDS = {
      0,       // Lv1  種
      300,     // Lv2  芽
      1_000,   // Lv3  苗
      2_500,   // Lv4  若木
      5_000,   // Lv5  成木
      9_500,   // Lv6  蕾
      15_600,  // Lv7  開花
      23_000,  // Lv8  結実
      32_000,  // Lv9  豊穣
      47_000   // Lv10 大樹
  };

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

    // 均等割り（端数切り捨て）
    int perTag = totalExp / tagIds.size();
    if (perTag <= 0) return results;

    for (Long tagId : tagIds) {
      SproutTagList tag = tagListDao.selectByTagId(tagId, userId);
      if (tag == null) continue;

      int oldLv  = tag.getLv();
      int newExp = tag.getExp() + perTag;
      int newLv  = calcLv(newExp);
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
  }

  // ===== private helpers =====

  /**
   * 累計EXPからレベルを計算する。
   * Lv10到達後もEXPは継続計測し、Lvは10で据え置く（将来ステージへの拡張を見越した設計）。
   *
   * @param totalExp 累計EXP
   * @return レベル（1〜10）
   */
  static int calcLv(int totalExp) {
    int lv = 1;
    for (int i = LV_THRESHOLDS.length - 1; i >= 0; i--) {
      if (totalExp >= LV_THRESHOLDS[i]) {
        lv = i + 1;
        break;
      }
    }
    return lv;
  }
}
