package com.example.sprout.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.sprout.dao.SproutItemListDao;
import com.example.sprout.model.SproutItemListDetail;
import com.example.sprout.security.AccessControlService;

@Service
public class SproutItemListServiceImpl implements SproutItemListService {

  @Autowired
  private SproutItemListDao sproutItemListDao;
  @Autowired
  private AccessControlService accessControlService;

  // 初期表示
  @Override
  public List<SproutItemListDetail> init() {
    Long userId = accessControlService.getLoginUserId();
    return sproutItemListDao.selectAll(userId);
  }

  // タスク情報取得
  @Override
  public SproutItemListDetail selectByItemId(Long id) {
    Long userId = accessControlService.getLoginUserId();
    return sproutItemListDao.selectByItemId(id, userId);
  }

  // 新規タスク登録
  @Override
  public Long insert(SproutItemListDetail model) {
    model.setUserId(accessControlService.getLoginUserId());
    model.setUpdateUser(accessControlService.getLoginId());
    model.setUpdateAt(LocalDateTime.now());

    sproutItemListDao.insert(model);
    return model.getId();
  }

  // タスク更新
  public void update(SproutItemListDetail model) {
    model.setUserId(accessControlService.getLoginUserId());
    model.setUpdateUser(accessControlService.getLoginId());
    model.setUpdateAt(LocalDateTime.now());

    sproutItemListDao.update(model);
  }

  // 削除
  public void delete(Long id) {
    Long userId = accessControlService.getLoginUserId();
    sproutItemListDao.delete(id, userId);
  }
}
