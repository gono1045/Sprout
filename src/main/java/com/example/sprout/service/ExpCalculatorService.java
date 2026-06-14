package com.example.sprout.service;

import java.util.List;

import com.example.sprout.model.TagExpResult;

/**
 * EXP計算・タグへの分配を担うサービス。
 * <p>
 * 将来のストリーク・難易度ボーナスもこのクラスに追加する。
 * </p>
 */
public interface ExpCalculatorService {

  /**
   * 工数と達成感ボーナスからEXP合計を計算する。
   *
   * <pre>
   * EXP = 工数(分) + 達成感ボーナス
   * ★1: +0  ★2: +5  ★3: +10  ★4: +20  ★5: +30
   * </pre>
   *
   * @param durationMin  計測時間（分）
   * @param satisfaction 達成感（1〜5）
   * @return 合計EXP
   */
  int calcExp(int durationMin, int satisfaction);

  /**
   * 計算したEXPをタグへ均等分配し、{@code sprout_tag} の exp / lv を更新する。
   * <p>
   * タグが複数の場合は端数切り捨てで均等割りする（例: 90EXP・3タグ → 各30EXP）。
   * </p>
   *
   * @param userId   ログインユーザーID
   * @param tagIds   付与対象のタグIDリスト
   * @param totalExp 付与するEXP合計
   * @return タグ別の EXP 付与結果リスト
   */
  List<TagExpResult> distributeExp(Long userId, List<Long> tagIds, int totalExp);
}
