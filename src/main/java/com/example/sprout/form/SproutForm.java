package com.example.sprout.form;

import java.time.LocalDate;
import java.util.List;

import com.example.sprout.model.SproutItem;

public class SproutForm {

  private Long id;
  private String title;
  private List<Long> tagIds;
  private String status;
  private String priority;
  private LocalDate createdAt;
  private LocalDate deadline;
  // 締切セルのHTML整形用
  private String deadlineHtml;
  private String detail;
  private Boolean done;

  // --- Getter / Setter ---
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<Long> getTagIds() {
    return tagIds;
  }

  public void setTagIds(List<Long> tagIds) {
    this.tagIds = tagIds;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public LocalDate getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDate createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDate getDeadline() {
    return deadline;
  }

  public void setDeadline(LocalDate deadline) {
    this.deadline = deadline;
  }

  /**
   * deadlineHtmlを取得する
   * 
   * @return deadlineHtml
   */
  public String getDeadlineHtml() {
    return deadlineHtml;
  }

  /**
   * deadlineHtmlを設定する
   * 
   * @param deadlineHtml
   * @return
   */
  public void setDeadlineHtml(String deadlineHtml) {
    this.deadlineHtml = deadlineHtml;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public Boolean getDone() {
    return done;
  }

  public void setDone(Boolean done) {
    this.done = done;
  }

  /**
   * Formの内容を既存のEntityにマージする
   */
  public void applyToEntity(SproutItem item) {
    if (this.title != null)
      item.setTitle(this.title);
    if (this.status != null)
      item.setStatus(this.status);
    if (this.priority != null)
      item.setPriority(this.priority);
    if (this.createdAt != null)
      item.setCreatedAt(this.createdAt);
    if (this.deadline != null)
      item.setDeadline(this.deadline);
    if (this.detail != null)
      item.setDetail(this.detail);
    if (this.done != null)
      item.setDone(this.done);
  }
}
