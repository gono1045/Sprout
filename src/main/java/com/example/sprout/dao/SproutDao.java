package com.example.sprout.dao;

import com.example.sprout.model.SproutItem;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SproutDao {

  List<SproutItem> findAll();

  SproutItem findById(@Param("id") Long id);

  void insert(SproutItem item);

  void update(SproutItem item);

  void delete(@Param("id") Long id);
}
