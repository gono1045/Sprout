package com.example.sprout.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

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
  SproutTagList insert(SproutTagList model);

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
   * タスクに紐づくタグを取得する
   * @param itemId
   * @return
   */
  List<SproutTagList> selectTagsByItemId(Long itemId);

  /**
   * ログインユーザーの全タスク分のタスクID→タグIDリストを一括取得する
   * （タスク件数分の個別取得によるN+1クエリを避けるため）
   * @return タスクIDをキーとしたタグID文字列リストのマップ
   */
  Map<Long, List<String>> selectAllItemTagIds();

  /**
   * タスクに紐づくタグを更新する
   * （既存は削除 → 新規登録）
   * @param itemId タスクID
   * @param tagIds 紐付けるタグIDリスト
   */
  void updateItemTags(Long itemId, List<Long> tagIds);

  /**
   * タスクとタグの紐付け削除
   * @param itemId タスクID
   * @param tagId タグID
   */
  void deleteItemTag(@Param("itemId") Long itemId, @Param("tagId") Long tagId);
}
