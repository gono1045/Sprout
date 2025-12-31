package com.example.sprout.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.sprout.model.SproutItemListDetail;

@Mapper
public interface SproutItemListDao {

  // 初期表示用
  List<SproutItemListDetail> selectAll();

  // タスク情報取得
  SproutItemListDetail selectByItemId(Long id);
}
