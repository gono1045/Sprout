package com.example.sprout.model;

import java.util.List;

/**
 * 工数記録の登録結果（フロントエンドへのレスポンス）
 */
public class WorkLogResult {

  /** 今回獲得した合計 EXP */
  private int expTotal;
  /** 計測時間（分） */
  private int durationMin;
  /** タグ別 EXP 付与結果 */
  private List<TagExpResult> tagResults;

  public int getExpTotal()   { return expTotal; }
  public void setExpTotal(int v) { this.expTotal = v; }

  public int getDurationMin()  { return durationMin; }
  public void setDurationMin(int v) { this.durationMin = v; }

  public List<TagExpResult> getTagResults() { return tagResults; }
  public void setTagResults(List<TagExpResult> v) { this.tagResults = v; }
}
