package com.example.sprout.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cglib.core.Local;

import com.example.sprout.form.SproutTagForm;
import com.example.sprout.model.SproutItem;
import com.example.sprout.model.SproutTag;

public interface SproutService {

  // Item関連
  List<SproutItem> getAllItems();

  SproutItem getItemById(Long id);

  void createItem(SproutItem item);

  void updateItem(SproutItem item);

  void deleteItem(Long id);

  public String buildDeadlineHtml(LocalDate deadline);

  // Tag関連
  List<SproutTag> getAllTags();

  SproutTag getTagById(Long tagId);

  void createTag(SproutTag tag);

  SproutTag updateTag(SproutTag tag);

  void deleteTag(Long tagId);

  // タスクにタグを設定 / 更新
  void assignTagsToItem(Long itemId, List<Long> tagIds);

  // タスクに設定されているタグを取得
  List<Long> getTagIdsByItemId(Long itemId);

  // タグ全体の一括保存（新規追加・選択状態・並び順）
  void saveTagsForItem(SproutTagForm form);
}
