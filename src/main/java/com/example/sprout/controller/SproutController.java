package com.example.sprout.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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
    model.addAttribute("sprouts", items);
    model.addAttribute("sproutForm", new SproutForm());
    model.addAttribute("showCompleted", false);
    return "index";
  }

  //新規作成
  @PostMapping("/add")
  public String addTask(@Valid @ModelAttribute SproutForm form, BindingResult result) {
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

      sproutDao.insert(item);
    }
    return "redirect:/";

  }

  //編集
  @PostMapping("/update")
  public String updateTask(@ModelAttribute SproutForm form) {
    SproutItem item = sproutDao.findById(form.getId());
    if (item != null) {
      item.setTitle(form.getTitle());
      item.setTag(form.getTag());
      item.setStatus(form.getStatus());
      item.setPriority(form.getPriority());
      item.setCreatedAt(form.getCreatedAt());
      item.setDeadline(form.getDeadline());
      item.setDetail(form.getDetail());
      item.setDone(form.getDone() != null ? form.getDone() : false);

      sproutDao.update(item);
    }
    return "redirect:/";

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