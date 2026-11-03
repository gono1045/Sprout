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
  /** 保存したワークログID（木の実保存時に利用） */
  private Long workLogId;
  /** タスクID（木の実保存時のタグ継承に利用） */
  private Long itemId;
  /** AI生成タイトル（メモなし or 要約失敗の場合は null） */
  private String aiTitle;
  /** AI要約テキスト（メモなし or 要約失敗の場合は null） */
  private String aiSummary;
  /** AI要約生成に失敗した場合 true（LM Studio 未起動等） */
  private boolean aiSummaryFailed;

  public int getExpTotal()   { return expTotal; }
  public void setExpTotal(int v) { this.expTotal = v; }

  public int getDurationMin()  { return durationMin; }
  public void setDurationMin(int v) { this.durationMin = v; }

  public List<TagExpResult> getTagResults() { return tagResults; }
  public void setTagResults(List<TagExpResult> v) { this.tagResults = v; }

  public Long getWorkLogId() { return workLogId; }
  public void setWorkLogId(Long v) { this.workLogId = v; }

  public Long getItemId() { return itemId; }
  public void setItemId(Long v) { this.itemId = v; }

  public String getAiTitle() { return aiTitle; }
  public void setAiTitle(String v) { this.aiTitle = v; }

  public String getAiSummary() { return aiSummary; }
  public void setAiSummary(String v) { this.aiSummary = v; }

  public boolean isAiSummaryFailed() { return aiSummaryFailed; }
  public void setAiSummaryFailed(boolean v) { this.aiSummaryFailed = v; }
}
