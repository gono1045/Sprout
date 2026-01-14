package com.example.sprout.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.sprout.form.SproutItemListForm;
import com.example.sprout.model.SproutItemListDetail;
import com.example.sprout.service.SproutItemListService;

@Controller
public class SproutItemListController {

  @Autowired
  private SproutItemListService sproutItemListService;

  /**
   * Top画面 初期表示
   * @return Top画面
   */
  @GetMapping("/")
  public String initTop(@ModelAttribute SproutItemListForm form, Model model) {

    // タスク一覧取得
    model.addAttribute("items", sproutItemListService.init());

    return "sproutTop";
  }

  /**
   * モーダル画面 新規登録・更新 初期表示
   *
   * form SproutItemListForm
   * modalFlg modalFlg
   * model Model
   */
  @GetMapping("/modal/update")
  public String initItemUpdateModal(@ModelAttribute SproutItemListForm form,
      @RequestParam(required = false) Integer modalFlg, Model model) {

    // 新規登録
    if (Integer.valueOf(0).equals(modalFlg)) {
      form.setStatusName("未着手");
      form.setPriorityName("低");
      form.setUpdateAt(LocalDateTime.now());
      model.addAttribute("sproutItemListForm", form);
      return "itemUpdateModal";
    }

    // 更新
    if (form.getId() != null) {
      SproutItemListDetail item = sproutItemListService.selectByItemId(form.getId());
      form.setDetailListFrom(item);
    }

    model.addAttribute("sproutItemListForm", form);

    return "itemUpdateModal";
  }

  /**
   * 新規タスク登録
   *
   */
  @PostMapping("/task/new")
  @ResponseBody
  public SproutItemListForm createNewTask(@ModelAttribute SproutItemListForm form) {

    form.setUpdateAt(LocalDateTime.now());
    SproutItemListDetail model = form.createModel();
    sproutItemListService.insert(model);

    form.setId(model.getId());
    return form;
  }

  /**
   * タスク更新
   *
   */
  @PostMapping("/task/update")
  @ResponseBody
  public SproutItemListForm updateTask(@ModelAttribute SproutItemListForm form) {

    form.setUpdateAt(LocalDateTime.now());
    sproutItemListService.update(form.createModel());

    return form;
  }

  /**
   * タスク一覧 再取得（非同期用）
   */
  @GetMapping("/task/list")
  @ResponseBody
  public List<SproutItemListForm> getTaskList() {

    return sproutItemListService.init().stream()
        .map(item -> {
          SproutItemListForm form = new SproutItemListForm();
          form.setDetailListFrom(item);
          return form;
        })
        .toList();
  }

  /**
   * タスク削除
   */
  @PostMapping("/task/delete")
  @ResponseBody
  public void delete(@RequestParam("id") Long id) {
    sproutItemListService.delete(id);
  }

  /**
   * タスク複製
   */
  @PostMapping("/task/duplicate")
  @ResponseBody
  public SproutItemListForm duplicateTask(@RequestParam("id") Long id) {

    // 複製元のタスク取得
    SproutItemListDetail original = sproutItemListService.selectByItemId(id);
    if (original == null) {
      throw new IllegalArgumentException("指定されたタスクが存在しません: " + id);
    }

    // 複製モデル
    SproutItemListForm duplicatedForm = new SproutItemListForm();
    duplicatedForm.setDetailListFrom(original);
    duplicatedForm.setUpdateAt(LocalDateTime.now());
    sproutItemListService.insert(duplicatedForm.createModel());

    return duplicatedForm;
  }
}