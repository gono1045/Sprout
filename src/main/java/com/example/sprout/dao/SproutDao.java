package com.example.sprout.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.sprout.model.SproutItem;
import com.example.sprout.model.SproutItemTag;
import com.example.sprout.model.SproutTag;

@Mapper
public interface SproutDao {

  //SproutItem
  List<SproutItem> findAll();

  SproutItem findById(@Param("id") Long id);

  void insert(SproutItem item);

  void update(SproutItem item);

  void delete(@Param("id") Long id);

  //Tags
  List<SproutTag> findAllTags();

  SproutTag findTagById(@Param("tagId") Long tagId);

  void insertTag(SproutTag tag);

  void updateTag(SproutTag tag);

  void deleteTag(@Param("tagId") Long tagId);

  void deleteItemTagsByItemId(Long itemId);

  void insertItemTag(SproutItemTag itemTag);

  List<SproutItemTag> findTagsByItemId(Long itemId);
}
