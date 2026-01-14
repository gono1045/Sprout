package com.example.sprout.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.sprout.dao.SproutItemListDao;
import com.example.sprout.model.SproutItemListDetail;

@Service
public class SproutItemListServiceImpl implements SproutItemListService {

  @Autowired
  private SproutItemListDao sproutItemListDao;

  // 初期表示
  @Override
  public List<SproutItemListDetail> init() {
    return sproutItemListDao.selectAll();
  }

  // タスク情報取得
  @Override
  public SproutItemListDetail selectByItemId(Long id) {
    return sproutItemListDao.selectByItemId(id);
  }

  // 新規タスク登録
  @Override
  public Long insert(SproutItemListDetail model) {
    sproutItemListDao.insert(model);
    return model.getId();
  }

  // タスク更新
  public void update(SproutItemListDetail model) {
    sproutItemListDao.update(model);
  }

  // 削除
  public void delete(Long id) {
    sproutItemListDao.delete(id);
  }
}
