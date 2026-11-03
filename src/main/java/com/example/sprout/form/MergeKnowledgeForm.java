package com.example.sprout.form;

import java.util.List;

/**
 * 知識の実マージ実行フォーム（フォームsubmit用）
 */
public class MergeKnowledgeForm {

  /** マージ対象の知識の実ID一覧 */
  private List<Long> ids;

  /** マージ後のタイトル */
  private String title;

  /** マージ後のAI要約 */
  private String aiSummary;

  /** 現在のソートキー（リダイレクト維持用） */
  private String sort;

  public List<Long> getIds() { return ids; }
  public void setIds(List<Long> ids) { this.ids = ids; }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public String getAiSummary() { return aiSummary; }
  public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }

  public String getSort() { return sort; }
  public void setSort(String sort) { this.sort = sort; }
}
