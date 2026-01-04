package com.example.sprout.model;

/**
 * タスクとタグの紐付け情報を表すModel
 */
public class SproutItemTag {

  /** 中間テーブルID（任意、連番） */
  private Long id;

  /** タスクID */
  private Long itemId;

  /** タグID */
  private Long tagId;

  /**
   * 中間テーブルIDを取得する
   * @return id
   */
  public Long getId() {
    return id;
  }

  /**
   * 中間テーブルIDを設定する
   * @param id 中間テーブルID
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * タスクIDを取得する
   * @return itemId
   */
  public Long getItemId() {
    return itemId;
  }

  /**
   * タスクIDを設定する
   * @param itemId タスクID
   */
  public void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  /**
   * タグIDを取得する
   * @return tagId
   */
  public Long getTagId() {
    return tagId;
  }

  /**
   * タグIDを設定する
   * @param tagId タグID
   */
  public void setTagId(Long tagId) {
    this.tagId = tagId;
  }
}
