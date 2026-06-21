package com.example.sprout.model;

import java.time.LocalDateTime;

import com.example.sprout.enums.SproutStage;

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
  /** ユーザーID **/
  private Long userId;
  /** 更新ユーザー **/
  private String updateUser;
  /** 累積EXP */
  private int exp;
  /** レベル */
  private int lv;

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

  /**
   * ユーザーIDを取得する
   * @return userId
   */
  public Long getUserId() {
    return userId;
  }

  /**
   * ユーザーIDを設定する
   * @param userId ユーザーID
   */
  public void setUserId(Long userId) {
    this.userId = userId;
  }

  /**
   * 更新ユーザーを取得する
   * @return updateUser
   */
  public String getUpdateUser() {
    return updateUser;
  }

  /**
   * 更新ユーザーを設定する
   * @param updateUser 更新ユーザー
   */
  public void setUpdateUser(String updateUser) {
    this.updateUser = updateUser;
  }

  /**
   * 累積EXPを取得する
   * @return exp
   */
  public int getExp() {
    return exp;
  }

  /**
   * 累積EXPを設定する
   * @param exp 累積EXP
   */
  public void setExp(int exp) {
    this.exp = exp;
  }

  /**
   * レベルを取得する
   * @return lv
   */
  public int getLv() {
    return lv;
  }

  /**
   * レベルを設定する
   * @param lv レベル
   */
  public void setLv(int lv) {
    this.lv = lv;
  }

  /** lv からステージ名を導出して返す（DBカラム非対応・JSON自動シリアライズ用）。 */
  public String getStageName() {
    return SproutStage.fromLv(lv).getStageName();
  }
}
