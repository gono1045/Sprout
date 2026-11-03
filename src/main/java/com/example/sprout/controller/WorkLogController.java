package com.example.sprout.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger log = LoggerFactory.getLogger(WorkLogController.class);

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
    log.info("[WorkLog] POST /work-log/save itemId={} durationMin={} hasMemo={}",
        form.getItemId(), form.getDurationMin(),
        form.getMemo() != null && !form.getMemo().isBlank());
    WorkLogResult result = workLogService.save(form);
    log.info("[WorkLog] 保存完了 workLogId={} expTotal={} hasAiSummary={}",
        result.getWorkLogId(), result.getExpTotal(), result.getAiSummary() != null);
    return result;
  }
}
