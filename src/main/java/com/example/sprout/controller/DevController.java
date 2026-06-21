package com.example.sprout.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.sprout.enums.SproutStage;
import com.example.sprout.model.SproutTagList;
import com.example.sprout.security.AccessControlService;
import com.example.sprout.dao.SproutTagListDao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 開発環境専用コントローラー。
 * local プロファイル時のみ有効。本番環境では一切起動しない。
 */
@Profile("local")
@Controller
@RequestMapping("/dev")
public class DevController {

  @Autowired
  private SproutTagListDao tagListDao;

  @Autowired
  private AccessControlService accessControlService;

  /**
   * タグの EXP を直接セットする（Lv アップテスト用）。
   *
   * 使用例:
   *   /dev/tag-exp?tagName=Java&exp=295   → Lv1（295/300）に設定
   *   /dev/tag-exp?tagName=Java&exp=995   → Lv2（995/1000）に設定
   *
   * @param tagName 対象タグ名
   * @param exp     セットする累計 EXP
   */
  @GetMapping("/tag-exp")
  @ResponseBody
  public Map<String, Object> setTagExp(
      @RequestParam("tagName") String tagName,
      @RequestParam("exp")     int    exp) {

    Long userId = accessControlService.getLoginUserId();
    List<SproutTagList> tags = tagListDao.selectAll(userId);

    SproutTagList target = tags.stream()
        .filter(t -> tagName.equals(t.getTagName()))
        .findFirst()
        .orElse(null);

    Map<String, Object> result = new LinkedHashMap<>();

    if (target == null) {
      result.put("success", false);
      result.put("message", "タグが見つかりません: " + tagName);
      result.put("availableTags", tags.stream().map(SproutTagList::getTagName).toList());
      return result;
    }

    int newLv = SproutStage.fromExp(exp).getLv();
    tagListDao.updateExp(target.getTagId(), userId, exp, newLv);

    SproutStage stage    = SproutStage.fromLv(newLv);
    int         nextExp  = stage.nextMinExp();
    int         remaining = nextExp > 0 ? nextExp - exp : 0;

    result.put("success",   true);
    result.put("tagName",   tagName);
    result.put("exp",       exp);
    result.put("lv",        newLv);
    result.put("stage",     stage.getStageName());
    result.put("nextLvExp", nextExp > 0 ? nextExp : "MAX");
    result.put("remaining", nextExp > 0 ? remaining + " EXP でLvアップ" : "最大Lv到達済み");
    return result;
  }

  /**
   * ログイン中ユーザーの全タグと現在の EXP / Lv を一覧表示する。
   */
  @GetMapping("/tags")
  @ResponseBody
  public List<Map<String, Object>> listTags() {
    Long userId = accessControlService.getLoginUserId();
    return tagListDao.selectAll(userId).stream()
        .map(t -> {
          Map<String, Object> m = new LinkedHashMap<>();
          m.put("tagId",   t.getTagId());
          m.put("tagName", t.getTagName());
          m.put("exp",     t.getExp());
          m.put("lv",      t.getLv());
          m.put("stage",   SproutStage.fromLv(t.getLv()).getStageName());
          return m;
        })
        .toList();
  }
}
