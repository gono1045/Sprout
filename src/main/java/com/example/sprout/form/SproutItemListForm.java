package com.example.sprout.form;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.example.sprout.model.SproutItemListDetail;

public class SproutItemListForm extends SproutAbstractForm<SproutItemListDetail> {

  /** タスクID */
  private Long id;
  /** タイトル */
  private String title;
  /** ステータスコード */
  private Integer statusCd;
  /** ステータス */
  private String statusName;
  /** 優先度コード */
  private Integer priorityCd;
  /** 優先度 */
  private String priorityName;
  /** 締切 */
  private LocalDate deadline;
  /** 詳細 */
  private String detail;
  /** 更新日 */
  private LocalDateTime updateAt;
  /** 更新日(文字列) */
  private String updateAtStr;
  /** モーダル用フラグ */
  private Integer modalFlg;

  /**
   * タスクIDを取得する
   * @return id
   */
  public Long getId() {
    return id;
  }

  /**
   * タスクIDを設定する
   * @param id タスクID
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * タイトルを取得する
   * @return title
   */
  public String getTitle() {
    return title;
  }

  /**
   * タイトルを設定する
   * @param title タイトル
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * ステータスコードを取得する
   * @return statusCd
   */
  public Integer getStatusCd() {
    return statusCd;
  }

  /**
   * ステータスコードを設定する
   * @param statusCd ステータスコード
   */
  public void setStatusCd(Integer statusCd) {
    this.statusCd = statusCd;
  }

  /**
   * ステータスを取得する
   * @return statusName
   */
  public String getStatusName() {
    return statusName;
  }

  /**
   * ステータスを設定する
   * @param statusName ステータス
   */
  public void setStatusName(String statusName) {
    this.statusName = statusName;
  }

  /**
   * 優先度コードを取得する
   * @return priorityCd
   */
  public Integer getPriorityCd() {
    return priorityCd;
  }

  /**
   * 優先度コードを設定する
   * @param priorityCd 優先度コード
   */
  public void setPriorityCd(Integer priorityCd) {
    this.priorityCd = priorityCd;
  }

  /**
   * 優先度を取得する
   * @return priorityName
   */
  public String getPriorityName() {
    return priorityName;
  }

  /**
   * 優先度を設定する
   * @param priorityName 優先度
   */
  public void setPriorityName(String priorityName) {
    this.priorityName = priorityName;
  }

  /**
   * 締切を取得する
   * @return deadline
   */
  public LocalDate getDeadline() {
    return deadline;
  }

  /**
   * 締切を設定する
   * @param deadline 締切
   */
  public void setDeadline(LocalDate deadline) {
    this.deadline = deadline;
  }

  /**
   * 詳細を取得する
   * @return detail
   */
  public String getDetail() {
    return detail;
  }

  /**
   * 詳細を設定する
   * @param detail 詳細
   */
  public void setDetail(String detail) {
    this.detail = detail;
  }

  /**
   * 更新日を取得する
   * @return updateAt
   */
  public LocalDateTime getUpdateAt() {
    return updateAt;
  }

  /**
   * 更新日を設定する
   * @param updateAt 更新日
   */
  public void setUpdateAt(LocalDateTime updateAt) {
    this.updateAt = updateAt;
  }

  /**
   * 更新日(文字列)を取得する
   * @return updateAtStr
   */
  public String getUpdateAtStr() {
    return updateAtStr;
  }

  /**
   * 更新日(文字列)を設定する
   * @param updateAtStr 更新日(文字列)
   */
  public void setUpdateAtStr(String updateAtStr) {
    this.updateAtStr = updateAtStr;
  }

  /**
   * モーダル用フラグを取得する
   * @return modalFlg
   */
  public Integer getModalFlg() {
    return modalFlg;
  }

  /**
   * モーダル用フラグを設定する
   * @param doneFlg モーダル用フラグ
   */
  public void setModalFlg(Integer modalFlg) {
    this.modalFlg = modalFlg;
  }

  @Override
  protected SproutItemListDetail newModel() {
    return new SproutItemListDetail();
  }

  @Override
  public void setDetailListFrom(Object source) {
    super.setDetailListFrom(source);

    // 作成・更新日の補正
    if (this.updateAt != null) {
      this.updateAtStr = this.updateAt.format(
          DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }
  }
}
