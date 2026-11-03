package com.example.sprout.form;

import java.util.List;

/**
 * 知識の実マージAI提案リクエスト（Ajax用）
 */
public class MergeSuggestForm {

  /** マージ対象の知識の実ID一覧 */
  private List<Long> ids;

  /** マージの観点ヒント（任意） */
  private String hint;

  /** 対象アイテムのタイトル一覧（JS側から渡す） */
  private List<String> titles;

  /** 対象アイテムの要約一覧（JS側から渡す） */
  private List<String> summaries;

  public List<Long> getIds() { return ids; }
  public void setIds(List<Long> ids) { this.ids = ids; }

  public String getHint() { return hint; }
  public void setHint(String hint) { this.hint = hint; }

  public List<String> getTitles() { return titles; }
  public void setTitles(List<String> titles) { this.titles = titles; }

  public List<String> getSummaries() { return summaries; }
  public void setSummaries(List<String> summaries) { this.summaries = summaries; }
}
