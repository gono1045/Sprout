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
 * {@link AiSummaryService} の本番環境実装。
 * <p>
 * Google Gemini API（{@code generateContent}）を呼び出す。
 * APIキーは環境変数 {@code GEMINI_API_KEY} で管理し、YAMLには平文で書かない。
 * </p>
 */
@Service
@Profile("prod")
public class AiSummaryServiceProdImpl implements AiSummaryService {

  private static final Logger log = LoggerFactory.getLogger(AiSummaryServiceProdImpl.class);

  private static final String GEMINI_BASE_URL =
      "https://generativelanguage.googleapis.com";

  private static final String SYSTEM_PROMPT =
      "ユーザーの作業メモから、タイトル（20文字以内）と要約（50文字以内）を日本語で生成してください。" +
      "必ず次のJSON形式のみで返してください（余計な説明・コードブロック不要）: " +
      "{\"title\":\"タイトル\",\"summary\":\"要約\"}";

  private static final String MERGE_SYSTEM_PROMPT =
      "複数の知識メモを統合し、タイトル（20文字以内）と要約（80文字以内）を日本語で生成してください。" +
      "必ず次のJSON形式のみで返してください（余計な説明・コードブロック不要）: " +
      "{\"title\":\"タイトル\",\"summary\":\"要約\"}";

  private final RestClient restClient;
  private final String apiKey;
  private final String model;
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public AiSummaryServiceProdImpl(
      @Value("${ai.gemini.api-key}") String apiKey,
      @Value("${ai.gemini.model:gemini-1.5-flash}") String model) {
    this.apiKey   = apiKey;
    this.model    = model;
    this.restClient = RestClient.builder()
        .baseUrl(GEMINI_BASE_URL)
        .build();
  }

  @Override
  public AiSummaryResult summarize(String memo) {
    if (memo == null || memo.isBlank()) {
      return new AiSummaryResult(null, null);
    }

    try {
      String prompt = SYSTEM_PROMPT + "\n\n" + memo;

      Map<String, Object> requestBody = Map.of(
          "contents", List.of(
              Map.of("parts", List.of(
                  Map.of("text", prompt)
              ))
          ),
          "generationConfig", Map.of("maxOutputTokens", 100)
      );

      @SuppressWarnings("unchecked")
      Map<String, Object> response = restClient.post()
          .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
          .header("Content-Type", "application/json")
          .body(requestBody)
          .retrieve()
          .body(Map.class);

      return parseResult(extractContent(response), memo);

    } catch (Exception e) {
      log.error("Gemini API 呼び出し失敗: {}", e.getMessage(), e);
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

      String prompt = MERGE_SYSTEM_PROMPT + "\n\n" + userContent;

      Map<String, Object> requestBody = Map.of(
          "contents", List.of(
              Map.of("parts", List.of(
                  Map.of("text", prompt)
              ))
          ),
          "generationConfig", Map.of("maxOutputTokens", 150)
      );

      @SuppressWarnings("unchecked")
      Map<String, Object> response = restClient.post()
          .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
          .header("Content-Type", "application/json")
          .body(requestBody)
          .retrieve()
          .body(Map.class);

      return parseResult(extractContent(response), titles.get(0));

    } catch (Exception e) {
      log.error("Gemini API 呼び出し失敗（mergeItems）: {}", e.getMessage(), e);
      return new AiSummaryResult(null, String.join(" / ", titles.stream()
          .filter(t -> t != null && !t.isBlank()).toList()));
    }
  }

  // ===== private =====

  @SuppressWarnings("unchecked")
  private String extractContent(Map<String, Object> response) {
    if (response == null) return "";
    var candidates = (List<Map<String, Object>>) response.get("candidates");
    if (candidates == null || candidates.isEmpty()) return "";
    var content = (Map<String, Object>) candidates.get(0).get("content");
    if (content == null) return "";
    var parts = (List<Map<String, Object>>) content.get("parts");
    if (parts == null || parts.isEmpty()) return "";
    Object text = parts.get(0).get("text");
    return text != null ? text.toString() : "";
  }

  @SuppressWarnings("unchecked")
  private AiSummaryResult parseResult(String json, String originalMemo) {
    if (json == null || json.isBlank()) return fallback(originalMemo);
    try {
      String cleaned = json.replaceAll("(?s)```[a-z]*\\s*", "").replaceAll("```", "").trim();
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
