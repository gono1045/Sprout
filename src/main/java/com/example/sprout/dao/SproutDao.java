package com.example.sprout.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.sprout.model.SproutItem;

@Mapper
public interface SproutDao {

  List<SproutItem> findAll();

  SproutItem findById(@Param("id") Long id);

  void insert(SproutItem item);

  void update(SproutItem item);

  void delete(@Param("id") Long id);
}
