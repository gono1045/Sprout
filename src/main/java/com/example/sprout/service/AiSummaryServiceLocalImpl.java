package com.example.sprout.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.sprout.model.AiSummaryResult;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link AiSummaryService} のローカル環境実装。
 * <p>
 * LM Studio の OpenAI 互換 API（{@code /v1/chat/completions}）を呼び出す。
 * LM Studio が未起動の場合はフォールバックとして元のメモをそのまま返す。
 * </p>
 */
@Service
@Profile("local")
public class AiSummaryServiceLocalImpl implements AiSummaryService {

  private static final Logger log = LoggerFactory.getLogger(AiSummaryServiceLocalImpl.class);

  private static final String SYSTEM_PROMPT =
      "ユーザーの作業メモから、タイトル（20文字以内）と要約（50文字以内）を日本語で生成してください。" +
      "必ず次のJSON形式のみで返してください（余計な説明・コードブロック不要）: " +
      "{\"title\":\"タイトル\",\"summary\":\"要約\"}";

  private static final String MERGE_SYSTEM_PROMPT =
      "複数の知識メモを統合し、タイトル（20文字以内）と要約（80文字以内）を日本語で生成してください。" +
      "必ず次のJSON形式のみで返してください（余計な説明・コードブロック不要）: " +
      "{\"title\":\"タイトル\",\"summary\":\"要約\"}";

  private final RestClient restClient;
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public AiSummaryServiceLocalImpl(
      @Value("${ai.lm-studio.base-url:http://localhost:1234}") String baseUrl) {
    this.restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build();
  }

  @Override
  public AiSummaryResult summarize(String memo) {
    if (memo == null || memo.isBlank()) {
      return new AiSummaryResult(null, null);
    }

    try {
      Map<String, Object> requestBody = Map.of(
          "model", "local-model",
          "messages", List.of(
              Map.of("role", "system", "content", SYSTEM_PROMPT),
              Map.of("role", "user",   "content", memo)
          ),
          "temperature", 0.3,
          "max_tokens", 100
      );

      @SuppressWarnings("unchecked")
      Map<String, Object> response = restClient.post()
          .uri("/v1/chat/completions")
          .header("Content-Type", "application/json")
          .body(requestBody)
          .retrieve()
          .body(Map.class);

      return parseResult(extractContent(response), memo);

    } catch (Exception e) {
      log.warn("LM Studio API 呼び出し失敗（未起動の可能性）: {}", e.getMessage());
      return fallback(memo);
    }
  }

  @Override
  public AiSummaryResult mergeItems(List<String> titles, List<String> summaries, String hint) {
    if (titles == null || titles.isEmpty()) {
      return new AiSummaryResult(null, null);
    }

    try {
      StringBuilder userContent = new StringBuilder();
      if (hint != null && !hint.isBlank()) {
        userContent.append("統合の観点: ").append(hint).append("\n\n");
      }
      for (int i = 0; i < titles.size(); i++) {
        userContent.append("【知識").append(i + 1).append("】\n");
        if (titles.get(i) != null && !titles.get(i).isBlank()) {
          userContent.append("タイトル: ").append(titles.get(i)).append("\n");
        }
        if (summaries != null && i < summaries.size() && summaries.get(i) != null) {
          userContent.append("要約: ").append(summaries.get(i)).append("\n");
        }
        userContent.append("\n");
      }

      Map<String, Object> requestBody = Map.of(
          "model", "local-model",
          "messages", List.of(
              Map.of("role", "system", "content", MERGE_SYSTEM_PROMPT),
              Map.of("role", "user",   "content", userContent.toString())
          ),
          "temperature", 0.3,
          "max_tokens", 150
      );

      @SuppressWarnings("unchecked")
      Map<String, Object> response = restClient.post()
          .uri("/v1/chat/completions")
          .header("Content-Type", "application/json")
          .body(requestBody)
          .retrieve()
          .body(Map.class);

      return parseResult(extractContent(response), titles.get(0));

    } catch (Exception e) {
      log.warn("LM Studio API 呼び出し失敗（mergeItems）: {}", e.getMessage());
      return new AiSummaryResult(null, String.join(" / ", titles.stream()
          .filter(t -> t != null && !t.isBlank()).toList()));
    }
  }

  // ===== private =====

  @SuppressWarnings("unchecked")
  private String extractContent(Map<String, Object> response) {
    if (response == null) return "";
    var choices = (List<Map<String, Object>>) response.get("choices");
    if (choices == null || choices.isEmpty()) return "";
    var message = (Map<String, Object>) choices.get(0).get("message");
    if (message == null) return "";
    Object content = message.get("content");
    return content != null ? content.toString() : "";
  }

  /** JSON文字列をパースしてAiSummaryResultに変換する。失敗時はフォールバック。 */
  @SuppressWarnings("unchecked")
  private AiSummaryResult parseResult(String json, String originalMemo) {
    if (json == null || json.isBlank()) return fallback(originalMemo);
    try {
      // コードブロック（```json ... ```）を除去
      String cleaned = json.replaceAll("(?s)```[a-z]*\\s*", "").replaceAll("```", "").trim();
      // JSON部分だけを抽出（最初の { から最後の } まで）
      int start = cleaned.indexOf('{');
      int end   = cleaned.lastIndexOf('}');
      if (start < 0 || end < 0) return fallback(originalMemo);
      Map<String, Object> map = MAPPER.readValue(cleaned.substring(start, end + 1), Map.class);
      String title   = map.getOrDefault("title",   "") != null ? map.get("title").toString()   : null;
      String summary = map.getOrDefault("summary", "") != null ? map.get("summary").toString() : null;
      return new AiSummaryResult(
          (title   != null && !title.isBlank())   ? title   : null,
          (summary != null && !summary.isBlank()) ? summary : fallback(originalMemo).getSummary()
      );
    } catch (Exception e) {
      log.warn("AI応答のJSONパース失敗: {}", e.getMessage());
      return fallback(originalMemo);
    }
  }

  private AiSummaryResult fallback(String memo) {
    String s = memo != null ? memo.trim() : "";
    return new AiSummaryResult(null, s.length() > 50 ? s.substring(0, 50) : s);
  }
}
