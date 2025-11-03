package com.example.sprout.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sprout_item_tag")
public class SproutItemTag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "item_id", nullable = false)
  private SproutItem item;

  @ManyToOne
  @JoinColumn(name = "tag_id", nullable = false)
  private SproutTag tag;

  // getter / setter
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public SproutItem getItem() {
    return item;
  }

  public void setItem(SproutItem item) {
    this.item = item;
  }

  public SproutTag getTag() {
    return tag;
  }

  public void setTag(SproutTag tag) {
    this.tag = tag;
  }
}
