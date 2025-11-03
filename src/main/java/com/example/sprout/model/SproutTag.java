package com.example.sprout.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tags") // DB テーブル名に合わせる
public class SproutTag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long tagId; // 識別子

  private String tagName; // タグ名
  private String tagDetail; // タグ詳細（必要に応じて）
  private String tagColor; // タグ色
  private Integer tagSortOrder; // 並び順

  private Long parentTagId; // 親タグ（親子構造用）

  // --- Getter / Setter ---
  public Long getTagId() {
    return tagId;
  }

  public void setTagId(Long tagId) {
    this.tagId = tagId;
  }

  public String getTagName() {
    return tagName;
  }

  public void setTagName(String tagName) {
    this.tagName = tagName;
  }

  public String getTagDetail() {
    return tagDetail;
  }

  public void setTagDetail(String tagDetail) {
    this.tagDetail = tagDetail;
  }

  public String getTagColor() {
    return tagColor;
  }

  public void setTagColor(String tagColor) {
    this.tagColor = tagColor;
  }

  public Integer getTagSortOrder() {
    return tagSortOrder;
  }

  public void setTagSortOrder(Integer tagSortOrder) {
    this.tagSortOrder = tagSortOrder;
  }

  public Long getParentTagId() {
    return parentTagId;
  }

  public void setParentTagId(Long parentTagId) {
    this.parentTagId = parentTagId;
  }
}
