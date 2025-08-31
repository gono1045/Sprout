package com.example.sprout.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sprout_item")
public class SproutItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String title;
  private String tag;
  private String status;
  private String priority;
  private LocalDate createdAt;
  private LocalDate deadline;
  private String detail;
  private Boolean done;

  /**
   * @return id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id セットする id
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title セットする title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return tag
   */
  public String getTag() {
    return tag;
  }

  /**
   * @param tag セットする tag
   */
  public void setTag(String tag) {
    this.tag = tag;
  }

  /**
   * @return status
   */
  public String getStatus() {
    return status;
  }

  /**
   * @param status セットする status
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * @return priority
   */
  public String getPriority() {
    return priority;
  }

  /**
   * @param priority セットする priority
   */
  public void setPriority(String priority) {
    this.priority = priority;
  }

  /**
   * @return createdAt
   */
  public LocalDate getCreatedAt() {
    return createdAt;
  }

  /**
   * @param createdAt セットする createdAt
   */
  public void setCreatedAt(LocalDate createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * @return deadline
   */
  public LocalDate getDeadline() {
    return deadline;
  }

  /**
   * @param deadline セットする deadline
   */
  public void setDeadline(LocalDate deadline) {
    this.deadline = deadline;
  }

  /**
   * @return detail
   */
  public String getDetail() {
    return detail;
  }

  /**
   * @param detail セットする detail
   */
  public void setDetail(String detail) {
    this.detail = detail;
  }

  /**
   * @return done
   */
  public Boolean getDone() {
    return done;
  }

  /**
   * @param done セットする done
   */
  public void setDone(Boolean done) {
    this.done = done;
  }

}