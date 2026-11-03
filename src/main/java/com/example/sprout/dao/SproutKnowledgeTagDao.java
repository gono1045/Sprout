package com.example.sprout.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.sprout.model.SproutKnowledgeTag;

/**
 * 知識の木の実 ⇔ タグ 中間テーブル DAO
 */
@Mapper
public interface SproutKnowledgeTagDao {

  /**
   * タグ紐付けを一括登録する
   * @param tags 登録するタグ紐付けリスト
   */
  void insertBatch(@Param("tags") List<SproutKnowledgeTag> tags);

  /**
   * 木の実IDに紐づくタグを全削除する
   * @param knowledgeId 木の実ID
   * @param userId      ユーザーID（所有チェック兼用）
   */
  void deleteByKnowledgeId(@Param("knowledgeId") Long knowledgeId,
                            @Param("userId") Long userId);

  /**
   * 木の実IDに紐づくタグID一覧を取得する
   * @param knowledgeId 木の実ID
   * @param userId      ユーザーID
   * @return タグ紐付けリスト
   */
  List<SproutKnowledgeTag> selectByKnowledgeId(@Param("knowledgeId") Long knowledgeId,
                                                @Param("userId") Long userId);

  /**
   * 複数の木の実IDに紐づくタグを一括削除する
   * @param ids    削除対象の木の実ID一覧
   * @param userId ユーザーID（所有チェック兼用）
   */
  void deleteByKnowledgeIds(@Param("ids") List<Long> ids, @Param("userId") Long userId);
}
