package com.example.sprout.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sprout.dao.SproutTagListDao;
import com.example.sprout.enums.SproutTagColor;
import com.example.sprout.model.SproutItemTag;
import com.example.sprout.model.SproutTagList;
import com.example.sprout.security.AccessControlService;

@Service
public class SproutTagListServiceImpl implements SproutTagListService {

  @Autowired
  private SproutTagListDao tagListDao;
  @Autowired
  private AccessControlService accessControlService;

  @Override
  public List<SproutTagList> selectAll() {
    Long userId = accessControlService.getLoginUserId();
    return tagListDao.selectAll(userId);
  }

  @Override
  public SproutTagList selectByTagId(Long tagId) {
    Long userId = accessControlService.getLoginUserId();
    return tagListDao.selectByTagId(tagId, userId);
  }

  @Override
  public SproutTagList insert(SproutTagList model) {
    Long userId = accessControlService.getLoginUserId();
    model.setUserId(userId);
    model.setUpdateUser(accessControlService.getLoginId());
    model.setUpdateAt(LocalDateTime.now());

    // タグ色をランダムで設定
    SproutTagColor sproutTagColor = SproutTagColor.random();
    model.setTagColor(sproutTagColor.getTailwindClass());

    // 並び順は最後尾に追加
    Integer maxOrder = tagListDao.selectMaxSortOrder(userId);
    model.setTagSortOrder(maxOrder == null ? 1 : maxOrder + 1);

    tagListDao.insert(model);
    return model;
  }

  @Override
  public void update(SproutTagList model) {
    model.setUserId(accessControlService.getLoginUserId());
    model.setUpdateUser(accessControlService.getLoginId());
    model.setUpdateAt(LocalDateTime.now());

    tagListDao.update(model);
  }

  @Override
  @Transactional
  public void delete(Long tagId) {
    Long userId = accessControlService.getLoginUserId();

    tagListDao.deleteByTagId(tagId, userId);
    tagListDao.delete(tagId, userId);
  }

  @Override
  public List<SproutTagList> selectTagsByItemId(Long itemId) {
    Long userId = accessControlService.getLoginUserId();

    return tagListDao.selectTagsByItemId(itemId, userId);
  }

  /**
   * タスクのタグを更新
   * 既存のタグは削除して新しいタグを登録
   */
  @Override
  @Transactional
  public void updateItemTags(Long itemId, List<Long> tagIds) {
    Long userId = accessControlService.getLoginUserId();

    // 既存紐付けを削除
    tagListDao.deleteItemTagsByItemId(itemId, userId);

    // 新規タグを紐付け
    if (tagIds != null && !tagIds.isEmpty()) {
      for (Long tagId : tagIds) {
        SproutItemTag itemTag = new SproutItemTag();
        itemTag.setItemId(itemId);
        itemTag.setTagId(tagId);
        tagListDao.insertItemTag(itemTag);
      }
    }
  }

  @Override
  @Transactional
  public void deleteItemTag(Long itemId, Long tagId) {
    Long userId = accessControlService.getLoginUserId();

    tagListDao.deleteItemTag(itemId, tagId, userId);
  }
}
