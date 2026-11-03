package com.example.sprout.model;

/**
 * AI要約生成結果。
 * <p>
 * タイトル（20文字以内）と要約本文（50文字以内）をセットで保持する。
 * </p>
 */
public class AiSummaryResult {

  /** 知識のタイトル（20文字以内） */
  private String title;
  /** 要約テキスト（50文字以内） */
  private String summary;
  /**
   * LM Studio 未起動等の理由で AI 生成に失敗し、フォールバック値を返している場合 {@code true}。
   */
  private boolean fallback;

  public AiSummaryResult() {}

  public AiSummaryResult(String title, String summary) {
    this.title   = title;
    this.summary = summary;
  }

  public AiSummaryResult(String title, String summary, boolean fallback) {
    this.title    = title;
    this.summary  = summary;
    this.fallback = fallback;
  }

  public String getTitle()              { return title; }
  public void setTitle(String title)    { this.title = title; }

  public String getSummary()              { return summary; }
  public void setSummary(String summary)  { this.summary = summary; }

  public boolean isFallback()              { return fallback; }
  public void setFallback(boolean fallback){ this.fallback = fallback; }
}
