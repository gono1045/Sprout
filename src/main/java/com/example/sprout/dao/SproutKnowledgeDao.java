package com.example.sprout.dao;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.sprout.model.KnowledgeListItem;
import com.example.sprout.model.SproutKnowledge;

/**
 * 知識の木の実 DAO
 */
@Mapper
public interface SproutKnowledgeDao {

  /**
   * 木の実を登録する
   * @param model 登録する木の実（insert後に id がセットされる）
   */
  void insert(SproutKnowledge model);

  /**
   * ユーザーの全木の実を取得する
   * @param userId ユーザーID
   * @return 木の実リスト（作成日時降順）
   */
  List<SproutKnowledge> selectByUserId(@Param("userId") Long userId);

  /**
   * ユーザーの木の実一覧をタスク名・タグ付きで取得する
   * @param userId  ユーザーID
   * @param sortKey ソートキー（"new"=新しい順, "old"=古い順, "tag"=タグ別）
   * @return 木の実一覧
   */
  List<KnowledgeListItem> selectListByUserId(@Param("userId") Long userId,
                                             @Param("sortKey") String sortKey);

  /**
   * 木の実のタイトルと要約を更新する
   * @param id        木の実ID
   * @param title     新しいタイトル
   * @param aiSummary 新しい要約テキスト
   * @param userId    ユーザーID（所有チェック兼用）
   */
  void update(@Param("id") Long id, @Param("title") String title,
              @Param("aiSummary") String aiSummary, @Param("userId") Long userId);

  /**
   * 木の実を削除する
   * @param id    木の実ID
   * @param userId ユーザーID（所有チェック兼用）
   */
  void deleteById(@Param("id") Long id, @Param("userId") Long userId);

  /**
   * 複数の木の実を一括削除する
   * @param ids    削除対象ID一覧
   * @param userId ユーザーID（所有チェック兼用）
   */
  void deleteByIds(@Param("ids") List<Long> ids, @Param("userId") Long userId);

  /**
   * 木の実を1件取得する（EXP制御フラグの確認用）
   * @param id     木の実ID
   * @param userId ユーザーID（所有チェック兼用）
   * @return 木の実エンティティ、存在しない場合は null
   */
  SproutKnowledge selectById(@Param("id") Long id, @Param("userId") Long userId);

  /**
   * 同一タスクに保存EXPが既に付与済みかどうかを確認する
   * @param itemId 対象タスクID
   * @param userId ユーザーID
   * @return 付与済みなら true
   */
  boolean existsTaskExpGivenByItemId(@Param("itemId") Long itemId, @Param("userId") Long userId);

  /**
   * 保存EXP付与済みフラグを TRUE にセットする
   * @param id     木の実ID
   * @param userId ユーザーID（所有チェック兼用）
   */
  void markTaskExpGiven(@Param("id") Long id, @Param("userId") Long userId);

  /**
   * 編集EXP最終付与日を更新する
   * @param id               木の実ID
   * @param lastEditedExpAt  付与日（今日）
   * @param userId           ユーザーID（所有チェック兼用）
   */
  void updateLastEditedExpAt(@Param("id") Long id,
                              @Param("lastEditedExpAt") LocalDate lastEditedExpAt,
                              @Param("userId") Long userId);
}
