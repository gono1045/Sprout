package com.example.sprout.model;

/**
 * タグへの EXP 付与結果（1タグ分）
 */
public class TagExpResult {

  private Long   tagId;
  private String tagName;
  private String tagColor;
  /** 今回付与した EXP */
  private int    expGained;
  /** 更新後の累積 EXP */
  private int    newExp;
  /** 更新後のレベル */
  private int    newLv;
  /** レベルアップしたか */
  private boolean leveledUp;

  public Long   getTagId()      { return tagId; }
  public void   setTagId(Long v)  { this.tagId = v; }

  public String getTagName()    { return tagName; }
  public void   setTagName(String v) { this.tagName = v; }

  public String getTagColor()   { return tagColor; }
  public void   setTagColor(String v) { this.tagColor = v; }

  public int  getExpGained()    { return expGained; }
  public void setExpGained(int v)  { this.expGained = v; }

  public int  getNewExp()       { return newExp; }
  public void setNewExp(int v)  { this.newExp = v; }

  public int  getNewLv()        { return newLv; }
  public void setNewLv(int v)   { this.newLv = v; }

  public boolean isLeveledUp()      { return leveledUp; }
  public void    setLeveledUp(boolean v) { this.leveledUp = v; }
}
