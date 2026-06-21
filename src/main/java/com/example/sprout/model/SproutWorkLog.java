package com.example.sprout.model;

import java.time.LocalDateTime;

/**
 * 工数記録ログ（sprout_work_log テーブル対応モデル）
 */
public class SproutWorkLog {

  /** ID */
  private Long id;
  /** タスクID */
  private Long itemId;
  /** ユーザーID */
  private Long userId;
  /** 計測開始日時 */
  private LocalDateTime startedAt;
  /** 計測終了日時 */
  private LocalDateTime endedAt;
  /** 計測時間（分、一時停止を除く） */
  private int durationMin;
  /** 達成感（1〜5） */
  private int satisfaction;
  /** 気づきメモ */
  private String memo;
  /** 付与した合計EXP */
  private int expTotal;
  /** 作成日時 */
  private LocalDateTime createdAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Long getItemId() { return itemId; }
  public void setItemId(Long itemId) { this.itemId = itemId; }

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }

  public LocalDateTime getStartedAt() { return startedAt; }
  public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

  public LocalDateTime getEndedAt() { return endedAt; }
  public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

  public int getDurationMin() { return durationMin; }
  public void setDurationMin(int durationMin) { this.durationMin = durationMin; }

  public int getSatisfaction() { return satisfaction; }
  public void setSatisfaction(int satisfaction) { this.satisfaction = satisfaction; }

  public String getMemo() { return memo; }
  public void setMemo(String memo) { this.memo = memo; }

  public int getExpTotal() { return expTotal; }
  public void setExpTotal(int expTotal) { this.expTotal = expTotal; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
