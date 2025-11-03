package com.example.sprout.form;

import java.util.List;

import com.example.sprout.model.SproutTag;

public class SproutTagForm {

  // --- 単一タグ更新用 ---
  private Long tagId;
  private String name;
  private String color;
  private Integer sortOrder;
  private Long parentTagId;

  // --- 複数タグ保存用 ---
  private Long itemId; // 対象のタスクID
  private List<Long> selectedTagIds; // 選択されているタグID
  private List<SproutTag> newTags; // 新しく追加されたタグ
  private List<Long> orderedTagIds; // 並べ替え後の全タグID

  // --- Getter / Setter ---
  public Long getTagId() {
    return tagId;
  }

  public void setTagId(Long tagId) {
    this.tagId = tagId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  public Long getParentTagId() {
    return parentTagId;
  }

  public void setParentTagId(Long parentTagId) {
    this.parentTagId = parentTagId;
  }

  public Long getItemId() {
    return itemId;
  }

  public void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  public List<Long> getSelectedTagIds() {
    return selectedTagIds;
  }

  public void setSelectedTagIds(List<Long> selectedTagIds) {
    this.selectedTagIds = selectedTagIds;
  }

  public List<SproutTag> getNewTags() {
    return newTags;
  }

  public void setNewTags(List<SproutTag> newTags) {
    this.newTags = newTags;
  }

  public List<Long> getOrderedTagIds() {
    return orderedTagIds;
  }

  public void setOrderedTagIds(List<Long> orderedTagIds) {
    this.orderedTagIds = orderedTagIds;
  }

  // 単一タグ更新用の変換
  public SproutTag toSproutTag() {
    SproutTag tag = new SproutTag();
    tag.setTagId(this.tagId);
    tag.setTagName(this.name);
    tag.setTagColor(this.color);
    tag.setTagSortOrder(this.sortOrder);
    tag.setParentTagId(this.parentTagId);
    return tag;
  }
}
