package com.example.sprout.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;

import com.example.sprout.form.WorkLogForm;
import com.example.sprout.model.WorkLogResult;
import com.example.sprout.service.WorkLogService;

/**
 * 工数記録コントローラー
 */
@Controller
@RequestMapping("/work-log")
public class WorkLogController {

  @Autowired
  private WorkLogService workLogService;

  /**
   * タイマーモーダル HTML を返す
   */
  @GetMapping("/modal")
  public String timerModal(@RequestParam("itemId") Long itemId,
                            @RequestParam("itemTitle") String itemTitle,
                            Model model) {
    model.addAttribute("itemId", itemId);
    model.addAttribute("itemTitle", itemTitle);
    return "timerModal :: modal";
  }

  /**
   * 工数ログを保存する
   */
  @PostMapping("/save")
  @ResponseBody
  public WorkLogResult save(@RequestBody WorkLogForm form) {
    return workLogService.save(form);
  }
}
