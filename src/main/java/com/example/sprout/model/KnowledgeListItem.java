package com.example.sprout.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知識の木の実一覧表示用 VO。
 * <p>
 * sprout_knowledge に タスク名・タグリストを結合したもの。
 * </p>
 */
public class KnowledgeListItem {

  private Long id;
  private Long userId;
  private Long workLogId;
  private Long itemId;
  /** 元タスクのタイトル */
  private String itemTitle;
  /** 知識のタイトル */
  private String title;
  private String aiSummary;
  private LocalDateTime createdAt;
  /** 紐づくタグ一覧 */
  private List<SproutTagList> tags;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }

  public Long getWorkLogId() { return workLogId; }
  public void setWorkLogId(Long workLogId) { this.workLogId = workLogId; }

  public Long getItemId() { return itemId; }
  public void setItemId(Long itemId) { this.itemId = itemId; }

  public String getItemTitle() { return itemTitle; }
  public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public String getAiSummary() { return aiSummary; }
  public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public List<SproutTagList> getTags() { return tags; }
  public void setTags(List<SproutTagList> tags) { this.tags = tags; }
}
