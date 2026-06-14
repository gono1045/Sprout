package com.example.sprout.form;

/**
 * 工数記録フォーム（フロントエンドからのリクエストボディ）
 */
public class WorkLogForm {

  /** タスクID */
  private Long itemId;
  /** 計測開始日時（ISO-8601文字列） */
  private String startedAt;
  /** 計測終了日時（ISO-8601文字列） */
  private String endedAt;
  /** 計測時間（分、一時停止を除く） */
  private int durationMin;
  /** 達成感（1〜5） */
  private int satisfaction;
  /** 気づきメモ */
  private String memo;

  public Long getItemId() { return itemId; }
  public void setItemId(Long itemId) { this.itemId = itemId; }

  public String getStartedAt() { return startedAt; }
  public void setStartedAt(String startedAt) { this.startedAt = startedAt; }

  public String getEndedAt() { return endedAt; }
  public void setEndedAt(String endedAt) { this.endedAt = endedAt; }

  public int getDurationMin() { return durationMin; }
  public void setDurationMin(int durationMin) { this.durationMin = durationMin; }

  public int getSatisfaction() { return satisfaction; }
  public void setSatisfaction(int satisfaction) { this.satisfaction = satisfaction; }

  public String getMemo() { return memo; }
  public void setMemo(String memo) { this.memo = memo; }
}
