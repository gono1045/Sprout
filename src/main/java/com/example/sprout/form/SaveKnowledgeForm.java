package com.example.sprout.form;

/**
 * 木の実保存リクエストフォーム（タイマーモーダルからの Ajax 呼び出し用）
 */
public class SaveKnowledgeForm {

  /** 元となるワークログID */
  private Long workLogId;
  /** タスクID */
  private Long itemId;
  /** 知識のタイトル（AI生成 or ユーザー編集） */
  private String title;
  /** AI要約テキスト */
  private String aiSummary;

  public Long getWorkLogId() { return workLogId; }
  public void setWorkLogId(Long workLogId) { this.workLogId = workLogId; }

  public Long getItemId() { return itemId; }
  public void setItemId(Long itemId) { this.itemId = itemId; }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public String getAiSummary() { return aiSummary; }
  public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
}
