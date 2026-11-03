package com.example.sprout.form;

import java.util.List;

/**
 * 知識の実一括削除フォーム（フォームsubmit用）
 */
public class DeleteBulkKnowledgeForm {

  /** 削除対象の知識の実ID一覧 */
  private List<Long> ids;

  /** 現在のソートキー（リダイレクト維持用） */
  private String sort;

  public List<Long> getIds() { return ids; }
  public void setIds(List<Long> ids) { this.ids = ids; }

  public String getSort() { return sort; }
  public void setSort(String sort) { this.sort = sort; }
}
