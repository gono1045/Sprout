package com.example.sprout.enums;

/**
 * タグの成長ステージ定義。
 * Lv・ステージ名・そのLvに必要な累計EXP閾値を管理する。
 * Lv10以降のステージは将来の拡張時にここへ追加する。
 */
public enum SproutStage {

  SEED       (1,  "種",   0),
  SPROUT     (2,  "芽",   300),
  SEEDLING   (3,  "苗",   1_000),
  SAPLING    (4,  "若木", 2_500),
  YOUNG_TREE (5,  "成木", 5_000),
  BUD        (6,  "蕾",   9_500),
  BLOOM      (7,  "開花", 15_600),
  FRUIT      (8,  "結実", 23_000),
  HARVEST    (9,  "豊穣", 32_000),
  GREAT_TREE (10, "大樹", 47_000);

  private final int lv;
  private final String stageName;
  private final int minExp;

  SproutStage(int lv, String stageName, int minExp) {
    this.lv        = lv;
    this.stageName = stageName;
    this.minExp    = minExp;
  }

  public int    getLv()        { return lv; }
  public String getStageName() { return stageName; }
  public int    getMinExp()    { return minExp; }

  /** 累計EXPからステージを返す。Lv10到達後は GREAT_TREE のまま。 */
  public static SproutStage fromExp(int totalExp) {
    SproutStage result = SEED;
    for (SproutStage s : values()) {
      if (totalExp >= s.minExp) result = s;
    }
    return result;
  }

  /** Lv番号からステージを返す。範囲外は SEED。 */
  public static SproutStage fromLv(int lv) {
    for (SproutStage s : values()) {
      if (s.lv == lv) return s;
    }
    return SEED;
  }

  /** 次ステージの最小EXPを返す（Lv10 の場合は -1）。 */
  public int nextMinExp() {
    SproutStage[] all = values();
    for (int i = 0; i < all.length - 1; i++) {
      if (all[i] == this) return all[i + 1].minExp;
    }
    return -1;
  }
}
