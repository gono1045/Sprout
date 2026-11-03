package com.example.sprout.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 知識の木の実エンティティ（sprout_knowledge テーブル）
 * <p>
 * タイマー完了時の気づきメモをAIで要約したものを保存する。
 * JPA により DDL が自動生成される。
 * </p>
 */
@Entity
@Table(name = "sprout_knowledge")
public class SproutKnowledge {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** ユーザーID */
  @Column(name = "user_id", nullable = false)
  private Long userId;

  /** 元WorkLogID（トレーサビリティ） */
  @Column(name = "work_log_id", nullable = false)
  private Long workLogId;

  /** 元タスクID */
  @Column(name = "item_id", nullable = false)
  private Long itemId;

  /** 知識のタイトル（20文字以内、AIが生成 or ユーザーが編集） */
  @Column(name = "title", length = 100, nullable = true)
  private String title;

  /** AI要約テキスト */
  @Column(name = "ai_summary", nullable = false, columnDefinition = "TEXT")
  private String aiSummary;

  /** 作成日時 */
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  /** 保存EXP付与済みフラグ（同一タスク1回限り制御） */
  @Column(name = "task_exp_given", nullable = false)
  private Boolean taskExpGiven = false;

  /** 編集EXP最終付与日（同日1回制御） */
  @Column(name = "last_edited_exp_at")
  private LocalDate lastEditedExpAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }

  public Long getWorkLogId() { return workLogId; }
  public void setWorkLogId(Long workLogId) { this.workLogId = workLogId; }

  public Long getItemId() { return itemId; }
  public void setItemId(Long itemId) { this.itemId = itemId; }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public String getAiSummary() { return aiSummary; }
  public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public Boolean getTaskExpGiven() { return taskExpGiven; }
  public void setTaskExpGiven(Boolean taskExpGiven) { this.taskExpGiven = taskExpGiven; }

  public LocalDate getLastEditedExpAt() { return lastEditedExpAt; }
  public void setLastEditedExpAt(LocalDate lastEditedExpAt) { this.lastEditedExpAt = lastEditedExpAt; }
}
