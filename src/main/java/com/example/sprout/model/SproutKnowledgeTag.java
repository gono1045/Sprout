package com.example.sprout.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 知識の木の実 ⇔ タグ 中間テーブルエンティティ（sprout_knowledge_tag）
 * <p>
 * タスクのタグを木の実作成時に自動引き継ぎする。
 * JPA により DDL が自動生成される。
 * </p>
 */
@Entity
@Table(name = "sprout_knowledge_tag")
public class SproutKnowledgeTag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 木の実ID（FK → sprout_knowledge.id） */
  @Column(name = "knowledge_id", nullable = false)
  private Long knowledgeId;

  /** タグID（FK → sprout_tag_list.tag_id） */
  @Column(name = "tag_id", nullable = false)
  private Long tagId;

  /** ユーザーID */
  @Column(name = "user_id", nullable = false)
  private Long userId;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Long getKnowledgeId() { return knowledgeId; }
  public void setKnowledgeId(Long knowledgeId) { this.knowledgeId = knowledgeId; }

  public Long getTagId() { return tagId; }
  public void setTagId(Long tagId) { this.tagId = tagId; }

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
}
