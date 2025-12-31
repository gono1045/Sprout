package com.example.sprout.form;

import org.springframework.beans.BeanUtils;

public abstract class SproutAbstractForm {

  /**
   * Model から Form に値をコピー
   * @param source コピー元の Model または Form
   */
  public void setDetailListFrom(Object source) {
    if (source == null)
      return;
    BeanUtils.copyProperties(source, this);
  }
}
