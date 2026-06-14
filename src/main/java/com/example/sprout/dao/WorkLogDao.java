package com.example.sprout.dao;

import org.apache.ibatis.annotations.Mapper;

import com.example.sprout.model.SproutWorkLog;

@Mapper
public interface WorkLogDao {

  /**
   * 工数ログを登録する
   * @param model 登録するログ情報
   */
  void insert(SproutWorkLog model);
}
