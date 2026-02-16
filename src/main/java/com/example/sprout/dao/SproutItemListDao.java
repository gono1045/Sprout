package com.example.sprout.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.sprout.model.SproutItemListDetail;

@Mapper
public interface SproutItemListDao {

  /**
   * タスク一覧取得（初期表示用）
   * @param userId ユーザーID
   * @return タスクリスト
   */
  List<SproutItemListDetail> selectAll(@Param("userId") Long userId);

  /**
   * タスク情報取得（タスクID指定）
   * @param id タスクID
   * @param userId ユーザーID
   * @return タスク情報
   */
  SproutItemListDetail selectByItemId(@Param("id") Long id, @Param("userId") Long userId);

  /**
   * タスク新規登録
   * @param model 登録するタスク情報
   */
  void insert(SproutItemListDetail model);

  /**
   * タスク更新
   * @param model 更新するタスク情報
   */
  void update(SproutItemListDetail model);

  /**
   * タスク削除
   * @param id タスクID
   * @param userId ユーザーID
   */
  void delete(@Param("id") Long id, @Param("userId") Long userId);
}