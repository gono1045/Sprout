package com.example.sprout.model;

public class SproutTag {

  private Long tagId;
  private String tagName;
  private String tagDetail;
  private String tagColor;
  private Integer tagSortOrder;
  private Long parentTagId;

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
