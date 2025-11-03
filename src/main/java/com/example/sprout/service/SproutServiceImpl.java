package com.example.sprout.service;

import java.time.LocalDate;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.example.sprout.dao.SproutDao;
import com.example.sprout.form.SproutTagForm;
import com.example.sprout.model.SproutItem;
import com.example.sprout.model.SproutItemTag;
import com.example.sprout.model.SproutTag;

@Service
public class SproutServiceImpl implements SproutService {

  private final SproutDao sproutDao;

  public SproutServiceImpl(SproutDao sproutDao) {
    this.sproutDao = sproutDao;
  }

  // Item
  @Override
  public List<SproutItem> getAllItems() {
    return sproutDao.findAll();
  }

  @Override
  public SproutItem getItemById(Long id) {
    return sproutDao.findById(id);
  }

  @Override
  public void createItem(SproutItem item) {
    sproutDao.insert(item);
  }

  @Override
  public void updateItem(SproutItem item) {
    sproutDao.update(item);
  }

  @Override
  public void deleteItem(Long id) {
    sproutDao.delete(id);
  }

  public String buildDeadlineHtml(LocalDate deadline) {
    if (deadline == null)
      return "(期限未設定)";
    LocalDate today = LocalDate.now();
    long diffDays = java.time.temporal.ChronoUnit.DAYS.between(today, deadline);
    String dateStr = deadline.format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    String colorClass, dayText;
    if (diffDays < 0) {
      colorClass = "text-red-500 font-bold";
      dayText = "(期限切れ " + Math.abs(diffDays) + "日)";
    } else if (diffDays == 0) {
      colorClass = "text-orange-500 font-bold";
      dayText = "(今日まで)";
    } else if (diffDays == 1) {
      colorClass = "text-orange-500";
      dayText = "(あと1日)";
    } else {
      colorClass = "text-gray-700 dark:text-gray-300";
      dayText = "(あと" + diffDays + "日)";
    }
    return String.format(
        "<div class='flex flex-col items-center'>" +
            "<span class='text-base text-gray-900 dark:text-blue-300 font-normal'>%s</span>" +
            "<span class='text-xs %s mt-1'>%s</span>" +
            "</div>",
        dateStr, colorClass, dayText);
  }

  // Tag
  @Override
  public List<SproutTag> getAllTags() {
    List<SproutTag> tags = sproutDao.findAllTags();
    return tags;
  }

  @Override
  public SproutTag getTagById(Long tagId) {
    return sproutDao.findTagById(tagId);
  }

  @Override
  public void createTag(SproutTag tag) {
    sproutDao.insertTag(tag);
  }

  @Override
  public void assignTagsToItem(Long itemId, List<Long> tagIds) {
    // 1. 既存のタグを削除
    sproutDao.deleteItemTagsByItemId(itemId);

    // 2. 新しいタグを中間テーブルに登録
    for (Long tagId : tagIds) {
      SproutItemTag itemTag = new SproutItemTag();
      SproutItem item = new SproutItem();
      item.setId(itemId);
      itemTag.setItem(item);

      SproutTag tag = new SproutTag();
      tag.setTagId(tagId);
      itemTag.setTag(tag);

      sproutDao.insertItemTag(itemTag);
    }
  }

  @Override
  public List<Long> getTagIdsByItemId(Long itemId) {
    List<SproutItemTag> itemTags = sproutDao.findTagsByItemId(itemId);
    return itemTags.stream()
        .map(it -> it.getTag().getTagId())
        .toList();
  }

  @Override
  public SproutTag updateTag(SproutTag tag) {
    SproutTag existing = sproutDao.findTagById(tag.getTagId());
    if (existing == null)
      return null;

    // 更新対象を既存モデルに反映
    existing.setTagName(tag.getTagName());
    existing.setTagColor(tag.getTagColor());
    existing.setTagSortOrder(tag.getTagSortOrder());
    existing.setParentTagId(tag.getParentTagId());
    existing.setTagDetail(tag.getTagDetail());

    sproutDao.updateTag(existing);

    return existing;
  }

  @Override
  public void deleteTag(Long tagId) {
    sproutDao.deleteTag(tagId);
  }

  @Transactional
  public void saveTagsForItem(SproutTagForm form) {
    Long itemId = form.getItemId();

    // 新規タグの保存（空タグは除外）
    if (form.getNewTags() != null) {
      for (SproutTag newTag : form.getNewTags()) {
        if (newTag.getTagName() != null && !newTag.getTagName().trim().isEmpty()) {
          createTag(newTag); // DB に保存
        }
      }
    }

    // タスクに紐づくタグを更新（選択状態）
    assignTagsToItem(itemId, form.getSelectedTagIds());

    // タグ全体の並び順更新
    if (form.getOrderedTagIds() != null) {
      int order = 1;
      for (Long tagId : form.getOrderedTagIds()) {
        SproutTag tag = getTagById(tagId);
        tag.setTagSortOrder(order++);
        updateTag(tag);
      }
    }
  }

}
