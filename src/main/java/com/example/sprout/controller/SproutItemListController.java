package com.example.sprout.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

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
   * モーダル画面 新規登録
   *
   */
  @GetMapping("/modal/update")
  public String initItemUpdateModal(@ModelAttribute SproutItemListForm form,
      @RequestParam(required = false) Integer modalFlg, Model model) {

    // 新規登録
    if (Integer.valueOf(0).equals(modalFlg)) {
      form.setStatusName("未着手");
      form.setPriorityName("低");
      form.setUpdateAt(LocalDateTime.now());
      String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
      form.setUpdateAtStr(todayStr);
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
}