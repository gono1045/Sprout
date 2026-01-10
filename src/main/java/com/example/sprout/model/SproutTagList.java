package com.example.sprout.model;

import java.time.LocalDateTime;

/**
 * タグ情報を表すModel
 */
public class SproutTagList {

  /** タグID */
  private Long tagId;

  /** タグ名 */
  private String tagName;

  /** タグ詳細 */
  private String tagDetail;

  /** タグ色 */
  private String tagColor;

  /** ソート順 */
  private Integer tagSortOrder;

  /** 親タグID */
  private Long parentTagId;

  /** 更新日時 */
  private LocalDateTime updateAt;

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

  /**
   * タグ名を取得する
   * @return tagName
   */
  public String getTagName() {
    return tagName;
  }

  /**
   * タグ名を設定する
   * @param tagName タグ名
   */
  public void setTagName(String tagName) {
    this.tagName = tagName;
  }

  /**
   * タグ詳細を取得する
   * @return tagDetail
   */
  public String getTagDetail() {
    return tagDetail;
  }

  /**
   * タグ詳細を設定する
   * @param tagDetail タグ詳細
   */
  public void setTagDetail(String tagDetail) {
    this.tagDetail = tagDetail;
  }

  /**
   * タグ色を取得する
   * @return tagColor
   */
  public String getTagColor() {
    return tagColor;
  }

  /**
   * タグ色を設定する
   * @param tagColor タグ色
   */
  public void setTagColor(String tagColor) {
    this.tagColor = tagColor;
  }

  /**
   * ソート順を取得する
   * @return tagSortOrder
   */
  public Integer getTagSortOrder() {
    return tagSortOrder;
  }

  /**
   * ソート順を設定する
   * @param tagSortOrder ソート順
   */
  public void setTagSortOrder(Integer tagSortOrder) {
    this.tagSortOrder = tagSortOrder;
  }

  /**
   * 親タグIDを取得する
   * @return parentTagId
   */
  public Long getParentTagId() {
    return parentTagId;
  }

  /**
   * 親タグIDを設定する
   * @param parentTagId 親タグID
   */
  public void setParentTagId(Long parentTagId) {
    this.parentTagId = parentTagId;
  }

  /**
   * 更新日時を取得する
   * @return updateAt
   */
  public LocalDateTime getUpdateAt() {
    return updateAt;
  }

  /**
   * 更新日時を設定する
   * @param updateAt 更新日時
   */
  public void setUpdateAt(LocalDateTime updateAt) {
    this.updateAt = updateAt;
  }
}
