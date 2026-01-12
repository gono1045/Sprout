package com.example.sprout.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sprout.dao.SproutTagListDao;
import com.example.sprout.enums.SproutTagColor;
import com.example.sprout.model.SproutItemTag;
import com.example.sprout.model.SproutTagList;

@Service
public class SproutTagListServiceImpl implements SproutTagListService {

  @Autowired
  private SproutTagListDao tagListDao;

  @Override
  public List<SproutTagList> selectAll() {
    return tagListDao.selectAll();
  }

  @Override
  public SproutTagList selectByTagId(Long tagId) {
    return tagListDao.selectByTagId(tagId);
  }

  @Override
  public SproutTagList insert(SproutTagList model) {

    // タグ色をランダムで設定
    SproutTagColor sproutTagColor = SproutTagColor.random();
    model.setTagColor(sproutTagColor.getTailwindClass());

    // 並び順は最後尾に追加
    Integer maxOrder = tagListDao.selectMaxSortOrder();
    model.setTagSortOrder(maxOrder == null ? 1 : maxOrder + 1);

    tagListDao.insert(model);
    return model;
  }

  @Override
  public void update(SproutTagList model) {
    tagListDao.update(model);
  }

  @Override
  @Transactional
  public void delete(Long tagId) {
    tagListDao.deleteByTagId(tagId);
    tagListDao.delete(tagId);
  }

  @Override
  @Transactional
  public void updateTagSortOrders(List<SproutTagList> tags) {
    for (SproutTagList tag : tags) {
      tagListDao.updateTagSortOrder(tag.getTagId(), tag.getTagSortOrder());
    }
  }

  @Override
  public List<SproutTagList> selectTagsByItemId(Long itemId) {
    return tagListDao.selectTagsByItemId(itemId);
  }

  /**
   * タスクのタグを更新
   * 既存のタグは削除して新しいタグを登録
   */
  @Override
  @Transactional
  public void updateItemTags(Long itemId, List<Long> tagIds) {

    // 既存紐付けを削除
    List<SproutTagList> existingTags = tagListDao.selectTagsByItemId(itemId);
    for (SproutTagList tag : existingTags) {
      tagListDao.deleteItemTag(itemId, tag.getTagId());
    }

    // 新規タグを紐付け
    for (Long tagId : tagIds) {
      SproutItemTag itemTag = new SproutItemTag();
      itemTag.setItemId(itemId);
      itemTag.setTagId(tagId);
      tagListDao.insertItemTag(itemTag);
    }
  }

  @Override
  @Transactional
  public void deleteItemTag(Long itemId, Long tagId) {
    tagListDao.deleteItemTag(itemId, tagId);
  }
}
