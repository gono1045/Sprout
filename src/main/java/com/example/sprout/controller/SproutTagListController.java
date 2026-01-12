package com.example.sprout.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.sprout.form.SproutTagListForm;
import com.example.sprout.model.SproutTagList;
import com.example.sprout.service.SproutTagListService;

@Controller
public class SproutTagListController {

  @Autowired
  private SproutTagListService sproutTagListService;

  /**
   * 全てのタグを取得する
   */
  @GetMapping("/tags/all")
  @ResponseBody
  public SproutTagListForm getAllTags(@ModelAttribute SproutTagListForm form) {

    List<SproutTagList> list = sproutTagListService.selectAll();
    form.setTagList(list);

    return form;
  }

  /**
   *  新規タグを作成する
   * @param form
   * @return
   */
  @PostMapping("/tags")
  @ResponseBody
  public SproutTagList create(@RequestBody SproutTagListForm form) {
    return sproutTagListService.insert(form.createModel());
  }

  /**
   * タグをタグテーブルと中間テーブルから削除する
   * @param tagId
   * @param itemId
   */
  @PostMapping("/tags/delete")
  @ResponseBody
  public void delete(@RequestParam("tagId") Long tagId) {
    sproutTagListService.delete(tagId);
  }

  /**
   * タグ情報を更新する
   * @param form
   */
  @PostMapping("/tags/update")
  @ResponseBody
  public void updateTag(@RequestBody SproutTagListForm form) {
    sproutTagListService.update(form.createModel());
  }

  /**
   * タスクに紐づくタグを取得する
   * @param itemId
   * @return
   */
  @GetMapping("/items/{itemId}/tags")
  @ResponseBody
  public List<SproutTagList> getItemTags(@PathVariable Long itemId) {
    return sproutTagListService.selectTagsByItemId(itemId);
  }

  /**
   * タスク紐づくタグを更新する
   * @param itemId
   * @param tagIds
   */
  @PostMapping("/items/{itemId}/tags")
  @ResponseBody
  public void updateItemTags(@PathVariable Long itemId, @RequestParam("tagIds") List<Long> tagIds) {
    sproutTagListService.updateItemTags(itemId, tagIds);
  }

}