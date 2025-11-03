package com.example.sprout.controller;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.sprout.form.SproutForm;
import com.example.sprout.form.SproutTagForm;
import com.example.sprout.model.SproutItem;
import com.example.sprout.model.SproutTag;
import com.example.sprout.service.SproutService;

@Controller
public class SproutController {

  private final SproutService sproutService;

  public SproutController(SproutService sproutService) {
    this.sproutService = sproutService;
  }

  // 初期表示
  @GetMapping("/")
  public String index(Model model) {
    List<SproutItem> items = sproutService.getAllItems();

    // タスクごとにタグ情報をセットする
    for (SproutItem item : items) {
      List<Long> tagIds = sproutService.getTagIdsByItemId(item.getId());
      List<SproutTag> tags = tagIds.stream()
          .map(id -> sproutService.getTagById(id))
          .toList();
      item.setTags(tags);
    }

    List<SproutForm> forms = items.stream().map(item -> {
      SproutForm form = new SproutForm();
      form.setId(item.getId());
      form.setTitle(item.getTitle());
      form.setStatus(item.getStatus());
      form.setPriority(item.getPriority());
      form.setCreatedAt(item.getCreatedAt());
      form.setDeadline(item.getDeadline());
      form.setDetail(item.getDetail());
      form.setDone(item.getDone());
      form.setTagIds(sproutService.getTagIdsByItemId(item.getId()));
      form.setDeadlineHtml(sproutService.buildDeadlineHtml(item.getDeadline()));
      return form;
    }).toList();

    model.addAttribute("sprouts", forms);
    model.addAttribute("sproutForm", new SproutForm());
    model.addAttribute("showCompleted", false);
    model.addAttribute("tagCandidates", sproutService.getAllTags());
    return "index";
  }

  // 新規作成
  @PostMapping("/add")
  @ResponseBody // AjaxでJSON返却
  public SproutItem addTask(@Valid @ModelAttribute SproutForm form, BindingResult result) {
    if (!result.hasErrors()) {
      SproutItem item = new SproutItem();
      item.setTitle(form.getTitle());
      item.setStatus(form.getStatus());
      item.setPriority(form.getPriority());
      item.setCreatedAt(form.getCreatedAt());
      item.setDeadline(form.getDeadline());
      item.setDetail(form.getDetail());
      item.setDone(false);

      sproutService.createItem(item); // DB登録
      sproutService.assignTagsToItem(item.getId(), form.getTagIds());
      return item; // 登録したタスクを返す
    }
    return null; // エラー時はnull返却

  }

  // 更新
  @PostMapping(value = "/update", produces = "application/json")
  @ResponseBody
  public SproutItem updateTask(@Valid @ModelAttribute SproutForm form, BindingResult result) {
    if (result.hasErrors()) {
      return null;
    }

    SproutItem item = sproutService.getItemById(form.getId());
    if (item != null) {
      form.applyToEntity(item);

      sproutService.updateItem(item);

      List<Long> tagIds = form.getTagIds() != null ? form.getTagIds() : new ArrayList<>();
      sproutService.assignTagsToItem(item.getId(), tagIds);
    }
    return item;
  }

  // 編集モーダル
  @GetMapping("edit/{id}")
  public String editTask(@PathVariable Long id, Model model) {
    SproutItem item = sproutService.getItemById(id);
    if (item == null) {
      return "redirect:/"; // データがない場合は一覧に戻る
    }

    SproutForm form = new SproutForm();
    form.setId(item.getId());
    form.setTitle(item.getTitle());
    form.setStatus(item.getStatus());
    form.setPriority(item.getPriority());
    form.setCreatedAt(item.getCreatedAt());
    form.setDeadline(item.getDeadline());
    form.setDetail(item.getDetail());
    form.setDone(item.getDone());

    model.addAttribute("sproutForm", form);
    model.addAttribute("editMode", true); // 編集用フラグ
    return "index"; // 一覧画面でモーダルを開く
  }

  // 削除
  @PostMapping("/delete/{id}")
  public String deleteTask(@PathVariable Long id) {
    sproutService.deleteItem(id);
    return "redirect:/";
  }

  // 完了トグル
  @PostMapping("/toggle/{id}")
  public String toggleDone(@PathVariable Long id) {
    SproutItem item = sproutService.getItemById(id);
    if (item != null) {
      item.setDone(!item.getDone());
      sproutService.updateItem(item);
    }
    return "redirect:/";
  }

  @GetMapping("/getAllTags")
  @ResponseBody
  public List<SproutTag> getAllTags() {
    return sproutService.getAllTags();
  }

  // 編集モード終了時に選択タグとリスト順序を保存
  @PostMapping("/saveTags")
  @ResponseBody
  public String saveTags(@RequestBody SproutTagForm form) {
    sproutService.saveTagsForItem(form);
    return "OK";
  }

  // タグ削除
  @PostMapping("/deleteTag")
  @ResponseBody
  public String deleteTag(@RequestParam Long tagId) {
    sproutService.deleteTag(tagId);
    return "OK";
  }

  // タグ更新（背景色や名前など単一タグ）
  @PostMapping("/updateTag")
  @ResponseBody
  public SproutTag updateTag(@Valid @ModelAttribute SproutTagForm form, BindingResult result) {
    if (result.hasErrors())
      return null;
    return sproutService.updateTag(form.toSproutTag());
  }

}