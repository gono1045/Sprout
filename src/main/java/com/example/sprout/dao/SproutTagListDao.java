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
   * @param userId ユーザーID
   * @return タグリスト
   */
  List<SproutTagList> selectAll(@Param("userId") Long userId);

  /**
   * タグ情報取得（タグID指定）
   * @param tagId タグID
   * @param userId ユーザーID
   * @return タグ情報
   */
  SproutTagList selectByTagId(@Param("tagId") Long tagId, @Param("userId") Long userId);

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
   * @param userId ユーザーID
   */
  void delete(@Param("tagId") Long tagId, @Param("userId") Long userId);

  /**
   * タグの並び順を更新
   * @param tagId
   * @param tagSortOrder
   * @param userId
   */
  void updateTagSortOrder(@Param("tagId") Long tagId, @Param("tagSortOrder") Integer tagSortOrder,
      @Param("userId") Long userId);

  /**
   * タグの並び順最大値を取得
   * @param userId ユーザーID
   * @return 最大値(データなしの場合null)
   */
  Integer selectMaxSortOrder(@Param("userId") Long userId);

  /**
   * タスクに紐づくタグ一覧取得
   * @param itemId タスクID
   * @param userId ユーザーID
   * @return タグリスト
   */
  List<SproutTagList> selectTagsByItemId(@Param("itemId") Long itemId, @Param("userId") Long userId);

  /**
   * ユーザーの全タスク分のタスク・タグ紐付けを一括取得する
   * （タスク件数分のN+1クエリを避けるための一括取得用）
   * @param userId ユーザーID
   * @return タスクID・タグIDの紐付けリスト
   */
  List<SproutItemTag> selectAllItemTags(@Param("userId") Long userId);

  /**
   * タスクとタグの紐付け登録
   * @param model タスクIDとタグID
   */
  void insertItemTag(SproutItemTag model);

  /**
   * タグ削除に伴う中間テーブルのレコード削除
   * @param tagId タグID
   * @param userId ユーザーID
   */
  void deleteByTagId(@Param("tagId") Long tagId, @Param("userId") Long userId);

  /**
   * タスクとタグの紐付け削除
   * @param itemId タスクID
   * @param tagId タグID
   * @param userId ユーザーID
   */
  void deleteItemTag(@Param("itemId") Long itemId, @Param("tagId") Long tagId, @Param("userId") Long userId);

  /**
   * タスクのタグを一気に解除
   * @param itemId
   * @param userId
   */
  void deleteItemTagsByItemId(@Param("itemId") Long itemId,
      @Param("userId") Long userId);

  /**
   * タグの exp / lv を更新する
   * @param tagId  タグID
   * @param userId ユーザーID
   * @param exp    更新後の累積EXP
   * @param lv     更新後のレベル
   */
  void updateExp(@Param("tagId") Long tagId, @Param("userId") Long userId,
      @Param("exp") int exp, @Param("lv") int lv);
}
