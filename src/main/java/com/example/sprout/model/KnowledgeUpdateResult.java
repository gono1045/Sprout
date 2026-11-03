package com.example.sprout.model;

import java.util.List;

/**
 * 知識の実 更新結果（フロントエンドへのレスポンス用）
 */
public class KnowledgeUpdateResult {

  /** タグへの EXP 分配結果（EXP が付与されなかった場合は空リスト） */
  private List<TagExpResult> expResults;
  /**
   * 同日編集 EXP 制限に達しているため EXP が付与されなかった場合 {@code true}。
   * タグなし・EXP付与失敗の場合は {@code false}。
   */
  private boolean expLimited;

  public KnowledgeUpdateResult(List<TagExpResult> expResults, boolean expLimited) {
    this.expResults = expResults;
    this.expLimited = expLimited;
  }

  public List<TagExpResult> getExpResults() { return expResults; }
  public boolean isExpLimited()             { return expLimited; }
}
