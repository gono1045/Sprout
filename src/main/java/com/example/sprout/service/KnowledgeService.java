package com.example.sprout.service;

import java.util.List;

import com.example.sprout.model.KnowledgeListItem;
import com.example.sprout.model.KnowledgeUpdateResult;
import com.example.sprout.model.TagExpResult;

/**
 * 知識の木の実サービス
 */
public interface KnowledgeService {

  /**
   * ログインユーザーの木の実一覧を取得する
   * @param sortKey ソートキー（"new" / "old" / "tag"）
   * @return 木の実一覧
   */
  List<KnowledgeListItem> getList(String sortKey);

  /**
   * 木の実を削除する
   * @param id 木の実ID
   */
  void delete(Long id);

  /**
   * ワークログから知識の実を保存する。
   * タスクに紐づくタグを sprout_knowledge_tag に継承する。
   *
   * @param workLogId  元となるワークログID
   * @param itemId     タスクID（タグ継承に使用）
   * @param title      知識のタイトル（null可）
   * @param aiSummary  AI要約テキスト
   */
  List<TagExpResult> saveFromWorkLog(Long workLogId, Long itemId, String title, String aiSummary);

  /**
   * 知識の実のタイトルと要約を更新する。
   *
   * @param id        木の実ID
   * @param title     新しいタイトル
   * @param aiSummary 新しい要約テキスト
   * @return 更新結果（EXP付与結果 + 同日制限フラグ）
   */
  KnowledgeUpdateResult update(Long id, String title, String aiSummary);

  /**
   * 複数の知識の実を1つにマージする。
   * 対象のタグを合算して新しい実を作成し、元の実を全件削除する。
   *
   * @param ids       マージ対象の木の実ID一覧
   * @param title     マージ後のタイトル
   * @param aiSummary マージ後の要約テキスト
   */
  List<TagExpResult> merge(List<Long> ids, String title, String aiSummary);

  /**
   * 複数の知識の実を一括削除する。
   *
   * @param ids 削除対象の木の実ID一覧
   */
  void deleteAll(List<Long> ids);
}
