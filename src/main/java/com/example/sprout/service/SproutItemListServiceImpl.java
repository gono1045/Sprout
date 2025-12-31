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
}
