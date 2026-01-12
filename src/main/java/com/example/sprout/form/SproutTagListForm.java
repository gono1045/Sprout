package com.example.sprout.form;

import java.time.LocalDateTime;
import java.util.List;

import com.example.sprout.model.SproutTagList;

public class SproutTagListForm extends SproutAbstractForm<SproutTagList> {

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

  /** タスクID */
  private Long itemId;

  /** 更新日時 */
  private LocalDateTime updateAt;

  /** タグリスト */
  private List<SproutTagList> tagList;

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
   * タスクIDを取得する
   * @return itemId
   */
  public Long getItemId() {
    return itemId;
  }

  /**
   * タスクIDを設定する
   * @param tagId タスクID
   */
  public void setItemId(Long itemId) {
    this.itemId = itemId;
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

  /**
   * タグリストを取得する
   * @return tagList
   */
  public List<SproutTagList> getTagList() {
    return tagList;
  }

  /**
   * タグリストを設定する
   * @param tagList タグリスト
   */
  public void setTagList(List<SproutTagList> tagList) {
    this.tagList = tagList;
  }

  @Override
  public SproutTagList createModel() {
    SproutTagList model = super.createModel();
    model.setUpdateAt(LocalDateTime.now());
    return model;
  }

  @Override
  protected SproutTagList newModel() {
    return new SproutTagList();
  }

}