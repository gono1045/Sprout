package com.example.sprout.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SproutItemListDetail {

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
}