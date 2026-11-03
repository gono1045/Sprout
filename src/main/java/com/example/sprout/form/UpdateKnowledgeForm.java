package com.example.sprout.form;

/**
 * 知識の実インライン編集リクエストフォーム（POST /knowledge/update 用）
 */
public class UpdateKnowledgeForm {

  /** 木の実ID */
  private Long id;
  /** タイトル（20文字以内） */
  private String title;
  /** AI要約テキスト */
  private String aiSummary;

  public Long getId()              { return id; }
  public void setId(Long id)       { this.id = id; }

  public String getTitle()              { return title; }
  public void setTitle(String title)    { this.title = title; }

  public String getAiSummary()              { return aiSummary; }
  public void setAiSummary(String aiSummary){ this.aiSummary = aiSummary; }
}
