package com.example.sprout.form;

import org.springframework.beans.BeanUtils;

public abstract class SproutAbstractForm<M> {

  /**
   * Model から Form に値をコピー
   * @param source コピー元の Model または Form
   */
  public void setDetailListFrom(Object source) {
    if (source == null)
      return;
    BeanUtils.copyProperties(source, this);
  }

  /**
   * FormからModelを生成
   */
  public M createModel() {
    M model = newModel();
    BeanUtils.copyProperties(this, model);
    return model;
  }

  /**
   * 各FormでModelのインスタンス生成を定義
   */
  protected abstract M newModel();
}
