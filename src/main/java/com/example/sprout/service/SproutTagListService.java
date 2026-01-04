package com.example.sprout.service;

import java.util.List;

import com.example.sprout.model.SproutTagList;

public interface SproutTagListService {

  /**
   * 全てのタグを取得する
   * @return
   */
  List<SproutTagList> selectAll();

  /**
   * タグを検索する
   * @param tagId
   * @return
   */
  SproutTagList selectByTagId(Long tagId);

  /**
   * 新規タグを登録する
   * @param model
   */
  void insert(SproutTagList model);

  /**
   * タグを更新する
   * @param model
   */
  void update(SproutTagList model);

  /**
   * タグを削除する
   * @param tagId
   */
  void delete(Long tagId);

  /**
   * タグの並び順を更新する
   * @param tags
   */
  void updateTagSortOrders(List<SproutTagList> tags);

  /**
   * タスクに紐づくタグを取得する
   * @param itemId
   * @return
   */
  List<SproutTagList> selectTagsByItemId(Long itemId);

  /**
   * タスクに紐づくタグを更新する
   * （既存は削除 → 新規登録）
   * @param itemId タスクID
   * @param tagIds 紐付けるタグIDリスト
   */
  void updateItemTags(Long itemId, List<Long> tagIds);
}
