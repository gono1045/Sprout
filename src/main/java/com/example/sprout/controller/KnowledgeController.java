package com.example.sprout.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sprout.model.KnowledgeListItem;
import com.example.sprout.model.SproutTagList;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.example.sprout.form.DeleteBulkKnowledgeForm;
import com.example.sprout.form.MergeKnowledgeForm;
import com.example.sprout.form.MergeSuggestForm;
import com.example.sprout.form.SaveKnowledgeForm;
import com.example.sprout.form.UpdateKnowledgeForm;
import com.example.sprout.model.AiSummaryResult;
import com.example.sprout.model.KnowledgeUpdateResult;
import com.example.sprout.model.TagExpResult;
import com.example.sprout.service.AiSummaryService;
import com.example.sprout.security.SproutUserDetails;
import com.example.sprout.service.KnowledgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 知識の木の実一覧画面コントローラー
 */
@Controller
@RequestMapping("/knowledge")
public class KnowledgeController {

  private static final Logger log = LoggerFactory.getLogger(KnowledgeController.class);

  @Autowired
  private KnowledgeService knowledgeService;

  @Autowired
  private AiSummaryService aiSummaryService;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  /**
   * 知識の実一覧画面を表示する
   * @param sortKey ソートキー（new / old / tag）、省略時は new
   */
  @GetMapping
  public String index(
      @RequestParam(value = "sort", defaultValue = "new") String sortKey,
      @AuthenticationPrincipal SproutUserDetails principal,
      Model model) {

    List<KnowledgeListItem> list = knowledgeService.getList(sortKey);
    model.addAttribute("knowledgeList", list);
    model.addAttribute("currentSort", sortKey);
    model.addAttribute("loginId", principal.getLoginId());

    // モーダル表示用: SproutTagList の全フィールドを避け、必要な項目のみMapに絞ってシリアライズ
    try {
      List<Map<String, Object>> jsonList = list.stream().map(item -> {
        Map<String, Object> m = new HashMap<>();
        m.put("id",        item.getId());
        m.put("title",     item.getTitle());
        m.put("aiSummary", item.getAiSummary());
        m.put("itemId",    item.getItemId());
        m.put("itemTitle", item.getItemTitle());
        m.put("createdAt", item.getCreatedAt() != null ? item.getCreatedAt().toString() : null);
        List<Map<String, Object>> tags = (item.getTags() == null) ? Collections.emptyList()
            : item.getTags().stream()
                .filter(t -> t != null && t.getTagId() != null)
                .map(t -> {
                  Map<String, Object> tm = new HashMap<>();
                  tm.put("tagId",    t.getTagId());
                  tm.put("tagName",  t.getTagName());
                  tm.put("tagColor", t.getTagColor());
                  return tm;
                }).collect(Collectors.toList());
        m.put("tags", tags);
        return m;
      }).collect(Collectors.toList());
      model.addAttribute("knItemsJson", OBJECT_MAPPER.writeValueAsString(jsonList));
    } catch (Exception e) {
      model.addAttribute("knItemsJson", "[]");
    }
    return "knowledge/index";
  }

  /**
   * ワークログから知識の実を保存する（タイマーモーダルからの Ajax 呼び出し）
   */
  @PostMapping("/save")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> save(@RequestBody SaveKnowledgeForm form) {
    List<TagExpResult> expResults = knowledgeService.saveFromWorkLog(
        form.getWorkLogId(), form.getItemId(), form.getTitle(), form.getAiSummary());
    Map<String, Object> resp = new java.util.HashMap<>();
    resp.put("expGained", expResults.stream().mapToInt(TagExpResult::getExpGained).sum());
    resp.put("levelUps",  expResults.stream()
        .filter(TagExpResult::isLeveledUp)
        .map(t -> t.getTagName() + " (Lv." + t.getNewLv() + ")")
        .collect(Collectors.toList()));
    return ResponseEntity.ok(resp);
  }

  /**
   * 知識の実のタイトル・要約をインライン編集する（一覧モーダルからの Ajax 呼び出し）
   */
  @PostMapping("/update")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> update(@RequestBody UpdateKnowledgeForm form) {
    KnowledgeUpdateResult updateResult = knowledgeService.update(form.getId(), form.getTitle(), form.getAiSummary());
    List<TagExpResult> expResults = updateResult.getExpResults();
    int expGained = expResults.stream().mapToInt(TagExpResult::getExpGained).sum();
    boolean expLimited = updateResult.isExpLimited();
    log.info("[Knowledge update] id={} expGained={} expLimited={}", form.getId(), expGained, expLimited);
    Map<String, Object> resp = new java.util.HashMap<>();
    resp.put("expGained",  expGained);
    resp.put("levelUps",   expResults.stream()
        .filter(TagExpResult::isLeveledUp)
        .map(t -> t.getTagName() + " (Lv." + t.getNewLv() + ")")
        .collect(Collectors.toList()));
    resp.put("expLimited", expLimited);
    return ResponseEntity.ok(resp);
  }

  /**
   * 木の実を削除する
   */
  @PostMapping("/delete")
  public String delete(
      @RequestParam("id") Long id,
      @RequestParam(value = "sort", defaultValue = "new") String sortKey,
      RedirectAttributes redirectAttrs) {

    knowledgeService.delete(id);
    redirectAttrs.addFlashAttribute("successMessage", "木の実を削除しました");
    return "redirect:/knowledge?sort=" + sortKey;
  }

  /**
   * 複数の木の実を一括削除する
   */
  @PostMapping("/delete-bulk")
  public String deleteBulk(
      @ModelAttribute DeleteBulkKnowledgeForm form,
      RedirectAttributes redirectAttrs) {

    if (form.getIds() != null && !form.getIds().isEmpty()) {
      knowledgeService.deleteAll(form.getIds());
      redirectAttrs.addFlashAttribute("successMessage",
          form.getIds().size() + "件の木の実を削除しました");
    }
    String sort = (form.getSort() != null && !form.getSort().isBlank()) ? form.getSort() : "new";
    return "redirect:/knowledge?sort=" + sort;
  }

  /**
   * マージ用AI提案を生成する（Ajax）
   */
  @PostMapping("/merge-suggest")
  @ResponseBody
  public ResponseEntity<Map<String, String>> mergeSuggest(@RequestBody MergeSuggestForm form) {
    AiSummaryResult result = aiSummaryService.mergeItems(
        form.getTitles(), form.getSummaries(), form.getHint());

    Map<String, String> response = new java.util.HashMap<>();
    response.put("title",     result.getTitle()   != null ? result.getTitle()   : "");
    response.put("aiSummary", result.getSummary()  != null ? result.getSummary() : "");
    return ResponseEntity.ok(response);
  }

  /**
   * 複数の木の実をマージして1つにまとめる
   */
  @PostMapping("/merge")
  public String merge(
      @ModelAttribute MergeKnowledgeForm form,
      RedirectAttributes redirectAttrs) {

    if (form.getIds() != null && form.getIds().size() >= 2) {
      List<TagExpResult> expResults = knowledgeService.merge(form.getIds(), form.getTitle(), form.getAiSummary());
      redirectAttrs.addFlashAttribute("successMessage",
          form.getIds().size() + "件の木の実をマージしました");
      int expGained = expResults.stream().mapToInt(TagExpResult::getExpGained).sum();
      if (expGained > 0) {
        redirectAttrs.addFlashAttribute("knExpGained", expGained);
      }
      List<String> levelUps = expResults.stream()
          .filter(TagExpResult::isLeveledUp)
          .map(t -> t.getTagName() + " (Lv." + t.getNewLv() + ")")
          .collect(Collectors.toList());
      if (!levelUps.isEmpty()) {
        redirectAttrs.addFlashAttribute("knLevelUps", levelUps);
      }
    }
    String sort = (form.getSort() != null && !form.getSort().isBlank()) ? form.getSort() : "new";
    return "redirect:/knowledge?sort=" + sort;
  }
}
