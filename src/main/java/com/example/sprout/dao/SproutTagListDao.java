package com.example.sprout.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.sprout.model.SproutItemTag;
import com.example.sprout.model.SproutTagList;

@Mapper
public interface SproutTagListDao {

  /**
   * タグ一覧取得（初期表示用）
   * @return タグリスト
   */
  List<SproutTagList> selectAll();

  /**
   * タグ情報取得（タグID指定）
   * @param tagId タグID
   * @return タグ情報
   */
  SproutTagList selectByTagId(Long tagId);

  /**
   * タグ新規登録
   * @param model 登録するタグ情報
   */
  void insert(SproutTagList model);

  /**
   * タグ更新
   * @param model 更新するタグ情報
   */
  void update(SproutTagList model);

  /**
   * タグ削除
   * @param tagId タグID
   */
  void delete(Long tagId);

  /**
   * タグの並び順を更新
   * @param tagId
   * @param tagSortOrder
   */
  void updateTagSortOrder(@Param("tagId") Long tagId, @Param("tagSortOrder") Integer tagSortOrder);

  /**
   * タスクに紐づくタグ一覧取得
   * @param itemId タスクID
   * @return タグリスト
   */
  List<SproutTagList> selectTagsByItemId(Long itemId);

  /**
   * タスクとタグの紐付け登録
   * @param model タスクIDとタグID
   */
  void insertItemTag(SproutItemTag model);

  /**
   * タスクとタグの紐付け削除
   * @param model タスクIDとタグID
   */
  void deleteItemTag(SproutItemTag model);
}
