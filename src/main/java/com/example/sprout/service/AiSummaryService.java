package com.example.sprout.service;

/**
 * AI要約サービスインターフェース。
 * <p>
 * 実装は Spring Profile で切り替える。
 * <ul>
 *   <li>{@code local} プロファイル: LM Studio（OpenAI互換API）</li>
 *   <li>{@code prod}  プロファイル: Gemini API</li>
 * </ul>
 * </p>
 */
public interface AiSummaryService {

  /**
   * 気づきメモを AI で要約し、タイトルと要約本文を生成する。
   *
   * @param memo 気づきメモ（タイマー停止時に入力されたテキスト）
   * @return タイトル（20文字以内）と要約（50文字以内）を持つ結果。
   *         API呼び出し失敗時は title=null, summary=memo先頭50文字 のフォールバック値を返す。
   */
  com.example.sprout.model.AiSummaryResult summarize(String memo);

  /**
   * 複数の知識の実を AI で統合し、新しいタイトルと要約本文を生成する。
   *
   * @param titles    マージ対象のタイトル一覧
   * @param summaries マージ対象の要約一覧
   * @param hint      マージの観点ヒント（任意、nullまたは空文字可）
   * @return 統合後のタイトル（20文字以内）と要約（80文字以内）を持つ結果。
   */
  com.example.sprout.model.AiSummaryResult mergeItems(
      java.util.List<String> titles,
      java.util.List<String> summaries,
      String hint);
}
