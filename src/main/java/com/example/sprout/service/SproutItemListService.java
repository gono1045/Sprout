package com.example.sprout.service;

import java.util.List;

import com.example.sprout.model.SproutItemListDetail;

public interface SproutItemListService {

  // 初期表示
  List<SproutItemListDetail> init();

  // タスク情報取得
  SproutItemListDetail selectByItemId(Long id);
}
