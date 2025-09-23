package com.example.sprout.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.sprout.dao.SproutDao;
import com.example.sprout.form.SproutForm;
import com.example.sprout.model.SproutItem;

@Controller
public class SproutController {

  private final SproutDao sproutDao;

  public SproutController(SproutDao sproutDao) {
    this.sproutDao = sproutDao;
  }

  //初期表示
  @GetMapping("/")
  public String index(Model model) {
    List<SproutItem> items = sproutDao.findAll();

    //既存タグ一覧を抽出(重複除去)
    Set<String> tagCandidates = items.stream()
        .map(SproutItem::getTag)
        .filter(Objects::nonNull)
        .filter(tag -> !tag.isBlank())
        .collect(Collectors.toCollection(TreeSet::new));//ソート機能

    model.addAttribute("sprouts", items);
    model.addAttribute("sproutForm", new SproutForm());
    model.addAttribute("showCompleted", false);
    model.addAttribute("tagCandidates", tagCandidates);
    return "index";
  }

  //新規作成
  @PostMapping("/add")
  @ResponseBody //AjaxでJSON返却
  public SproutItem addTask(@Valid @ModelAttribute SproutForm form, BindingResult result) {
    if (!result.hasErrors()) {
      SproutItem item = new SproutItem();
      item.setTitle(form.getTitle());
      item.setTag(form.getTag());
      item.setStatus(form.getStatus());
      item.setPriority(form.getPriority());
      item.setCreatedAt(form.getCreatedAt());
      item.setDeadline(form.getDeadline());
      item.setDetail(form.getDetail());
      item.setDone(false);

      sproutDao.insert(item); //DB登録
      return item; //登録したタスクを返す
    }
    return null; //エラー時はnull返却

  }

  @PostMapping(value = "/update", produces = "application/json")
  @ResponseBody
  public SproutItem updateTask(@RequestParam Long id,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String tag,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String priority,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createdAt,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate deadline,
      @RequestParam(required = false) String detail) {
    SproutItem item = sproutDao.findById(id);
    if (item != null) {
      if (title != null)
        item.setTitle(title);
      if (tag != null)
        item.setTag(tag);
      if (status != null)
        item.setStatus(status);
      if (priority != null)
        item.setPriority(priority);
      if (createdAt != null)
        item.setCreatedAt(createdAt);
      if (deadline != null)
        item.setDeadline(deadline);
      if (detail != null)
        item.setDetail(detail);

      sproutDao.update(item);
    }
    return item;
  }

  //編集モーダル
  @GetMapping("edit/{id}")
  public String editTask(@PathVariable Long id, Model model) {
    SproutItem item = sproutDao.findById(id);
    if (item == null) {
      return "redirect:/"; //データがない場合は一覧に戻る
    }

    SproutForm form = new SproutForm();
    form.setId(item.getId());
    form.setTitle(item.getTitle());
    form.setTag(item.getTag());
    form.setStatus(item.getStatus());
    form.setPriority(item.getPriority());
    form.setCreatedAt(item.getCreatedAt());
    form.setDeadline(item.getDeadline());
    form.setDetail(item.getDetail());
    form.setDone(item.getDone());

    model.addAttribute("sproutForm", form);
    model.addAttribute("editMode", true); //編集用フラグ
    return "index"; //一覧画面でモーダルを開く
  }

  //削除
  @PostMapping("/delete/{id}")
  public String deleteTask(@PathVariable Long id) {
    sproutDao.delete(id);
    return "redirect:/";
  }

  //完了トグル
  @PostMapping("/toggle/{id}")
  public String toggleDone(@PathVariable Long id) {
    SproutItem item = sproutDao.findById(id);
    if (item != null) {
      item.setDone(!item.getDone());
      sproutDao.update(item);
    }
    return "redirect:/";

  }
}